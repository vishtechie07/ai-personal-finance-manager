# API smoke test for SpendSense features (run against http://localhost:8080)
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080/api"
$failures = @()
$script:passed = 0

function Test-Step($name, [scriptblock]$Action) {
    try {
        & $Action
        Write-Host "[PASS] $name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "[FAIL] $name - $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) { Write-Host "  $($_.ErrorDetails.Message)" -ForegroundColor DarkRed }
        $script:failures += $name
    }
}

# POST JSON via Invoke-RestMethod (avoid ConvertTo-Json: pretty-print breaks nginx auth parsing)
function Invoke-ApiPost($path, [string]$JsonBody) {
    Invoke-RestMethod -Uri "$base$path" -Method POST -Headers $script:headers `
        -ContentType "application/json; charset=utf-8" -Body $JsonBody
}

Write-Host "`n=== SpendSense feature smoke test ===`n"

$script:headers = @{}

Test-Step "Login (spendsense)" {
    $login = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType "application/json" `
        -Body '{"username":"spendsense","password":"TrySpend2026!"}'
    if (-not $login.token) { throw "No token" }
    $script:headers = @{ Authorization = "Bearer $($login.token)" }
}

Test-Step "GET /auth/me" {
    $me = Invoke-RestMethod -Uri "$base/auth/me" -Headers $script:headers
    if (-not $me.username) { throw "Missing username" }
}

Test-Step "GET /bills (no Hibernate proxy crash)" {
    $bills = Invoke-RestMethod -Uri "$base/bills" -Headers $script:headers
    if ($null -eq $bills) { throw "Not an array" }
}

Test-Step "GET /bills/detect-recurring" {
    $rows = Invoke-RestMethod -Uri "$base/bills/detect-recurring" -Headers $script:headers
    if ($rows -isnot [Array]) { throw "Expected array" }
}

Test-Step "GET /notifications" {
    $notes = Invoke-RestMethod -Uri "$base/notifications" -Headers $script:headers
    if ($notes -isnot [Array]) { throw "Expected array" }
}

Test-Step "POST /notifications/sync" {
    Invoke-RestMethod -Uri "$base/notifications/sync" -Method POST -Headers $script:headers | Out-Null
}

Test-Step "POST /ai/monthly-brief" {
    $ym = (Get-Date).ToString("yyyy-MM")
    $brief = Invoke-ApiPost "/ai/monthly-brief" "{`"yearMonth`":`"$ym`"}"
    if (-not $brief.source) { throw "Missing source" }
    if ($brief.bullets -isnot [Array]) { throw "Missing bullets" }
}

Test-Step "POST /ai/suggest-category" {
    $s = Invoke-ApiPost "/ai/suggest-category" '{"description":"Starbucks coffee"}'
    if (-not $s.source) { throw "Missing source" }
}

Test-Step "GET /settings" {
    $s = Invoke-RestMethod -Uri "$base/settings" -Headers $script:headers
    if ($null -eq $s.hasOpenAiApiKey) { throw "Missing hasOpenAiApiKey" }
}

Test-Step "GET /transactions" {
    $tx = Invoke-RestMethod -Uri "$base/transactions" -Headers $script:headers
    if ($tx -isnot [Array]) { throw "Expected array" }
}

Test-Step "POST /transactions (create)" {
    $ts = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
    $created = Invoke-ApiPost "/transactions" "{`"description`":`"API probe lunch`",`"amount`":4.25,`"type`":`"EXPENSE`",`"category`":`"FOOD_RESTAURANT`",`"transactionDate`":`"$ts`"}"
    if (-not $created.id) { throw "No id returned" }
    $script:createdTxId = $created.id
}

Test-Step "POST /budgets (create)" {
    $ts = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
    $b = Invoke-ApiPost "/budgets" "{`"name`":`"API probe groceries`",`"amount`":250,`"category`":`"FOOD_GROCERIES`",`"period`":`"MONTHLY`",`"startDate`":`"$ts`"}"
    if (-not $b.id) { throw "No id returned" }
}

Test-Step "POST /bills (create)" {
    $due = (Get-Date).AddDays(14).ToString("yyyy-MM-dd")
    $bill = Invoke-ApiPost "/bills" "{`"payeeName`":`"Local utility`",`"amount`":19.99,`"dueDate`":`"$due`"}"
    if (-not $bill.id) { throw "No id returned" }
    $script:createdBillId = $bill.id
}

Test-Step "POST /bills/{id}/mark-paid (linked tx, list still serializes)" {
    Invoke-RestMethod -Uri "$base/bills/$($script:createdBillId)/mark-paid" -Method POST `
        -Headers $script:headers -ContentType "application/json" -Body '{"createTransaction":true}' | Out-Null
    $bills = Invoke-RestMethod -Uri "$base/bills" -Headers $script:headers
    if ($null -eq $bills) { throw "GET /bills failed after mark-paid" }
}

Test-Step "GET /config/public" {
    $cfg = Invoke-RestMethod -Uri "$base/config/public" -Method GET
    if ($null -eq $cfg.googleSignInEnabled) { throw "Missing googleSignInEnabled" }
}

Write-Host "`n=== Summary: $passed passed, $($failures.Count) failed ==="
if ($failures.Count -gt 0) {
    Write-Host "Failed:" ($failures -join ", ")
    exit 1
}
exit 0
