/**
 * Capture README screenshots from a running dev stack (backend :8080, frontend :3000).
 * Usage: node scripts/capture-screenshots.mjs
 */
import { chromium } from "playwright";
import { mkdir } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const outDir = path.resolve(__dirname, "../docs/screenshots");
const baseUrl = process.env.SCREENSHOT_BASE_URL || "http://localhost:3000";
const apiUrl = process.env.SCREENSHOT_API_URL || "http://localhost:8080/api";

const pages = [
  { name: "home", path: "/", auth: false, waitFor: "text=Get Started Free" },
  { name: "dashboard", path: "/dashboard", auth: true, waitFor: "text=Net balance" },
  { name: "transactions", path: "/transactions", auth: true, waitFor: "text=Export CSV" },
  { name: "budgets", path: "/budgets", auth: true, waitFor: "text=Budgets" },
  { name: "bills", path: "/bills", auth: true, waitFor: "text=Bills" },
  { name: "insights", path: "/insights", auth: true, waitFor: "text=Financial Insights" },
  { name: "settings", path: "/settings", auth: true, waitFor: "text=Settings" },
];

async function getAuthToken() {
  const res = await fetch(`${apiUrl}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "spendsense", password: "TrySpend2026!" }),
  });
  if (!res.ok) throw new Error(`Login failed: ${res.status}`);
  const data = await res.json();
  return data.token;
}

async function main() {
  await mkdir(outDir, { recursive: true });
  const token = await getAuthToken();

  const browser = await chromium.launch();
  const guestContext = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });
  const authContext = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });
  await authContext.addInitScript((t) => {
    localStorage.setItem("token", t);
  }, token);

  for (const shot of pages) {
    const context = shot.auth ? authContext : guestContext;
    const page = await context.newPage();
    await page.goto(`${baseUrl}${shot.path}`, { waitUntil: "networkidle" });
    await page.waitForSelector(shot.waitFor, { timeout: 30000 }).catch(() => {});

    // Dismiss onboarding overlay if present
    const dismiss = page.getByRole("button", { name: /skip|done|close|got it/i });
    if (await dismiss.count()) {
      await dismiss.first().click({ timeout: 2000 }).catch(() => {});
    }

    await page.waitForTimeout(800);
    await page.screenshot({
      path: path.join(outDir, `${shot.name}.png`),
      fullPage: shot.name === "home",
    });
    console.log(`Saved ${shot.name}.png`);
    await page.close();
  }

  await browser.close();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
