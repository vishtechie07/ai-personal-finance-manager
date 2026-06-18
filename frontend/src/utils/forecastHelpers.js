/**
 * Client-side projections from transaction/budget data (no ML).
 */

function daysInMonth(month, year) {
  return new Date(year, month + 1, 0).getDate();
}

export function monthEndExpenseForecast(expenses, month, year, referenceDate = new Date()) {
  const total = Number(expenses) || 0;
  if (total <= 0) return null;

  const isViewingCurrentMonth =
    referenceDate.getMonth() === month && referenceDate.getFullYear() === year;

  const dim = daysInMonth(month, year);
  const dayOfMonth = isViewingCurrentMonth ? referenceDate.getDate() : dim;
  if (dayOfMonth <= 0) return null;

  const dailyRate = total / dayOfMonth;
  return Math.round(dailyRate * dim * 100) / 100;
}

export function budgetBurnInsights(budgets, referenceDate = new Date()) {
  const dayOfMonth = referenceDate.getDate();
  const dim = daysInMonth(referenceDate.getMonth(), referenceDate.getFullYear());
  const daysLeftInMonth = Math.max(dim - dayOfMonth, 0);

  return (budgets || [])
    .map((b) => {
      const spent = parseFloat(b.spentAmount || 0);
      const cap = parseFloat(b.amount || 0);
      const remaining = cap - spent;
      if (cap <= 0) return null;

      if (remaining <= 0) {
        return {
          id: b.id,
          name: b.name,
          daysLeft: 0,
          daysLeftInMonth,
          message: "Over budget",
          tone: "danger",
        };
      }

      if (spent <= 0 || dayOfMonth <= 0) {
        return {
          id: b.id,
          name: b.name,
          daysLeft: null,
          daysLeftInMonth,
          message: "No spending yet",
          tone: "neutral",
        };
      }

      const dailyBurn = spent / dayOfMonth;
      const daysUntilEmpty = Math.floor(remaining / dailyBurn);
      const tone =
        daysUntilEmpty < daysLeftInMonth ? "warning" : "positive";

      return {
        id: b.id,
        name: b.name,
        daysLeft: daysUntilEmpty,
        daysLeftInMonth,
        message:
          daysUntilEmpty >= daysLeftInMonth
            ? `On track — ~${daysUntilEmpty} days of budget left at this pace`
            : `May run out in ~${daysUntilEmpty} days at this pace`,
        tone,
      };
    })
    .filter(Boolean)
    .sort((a, b) => (a.daysLeft ?? 999) - (b.daysLeft ?? 999))
    .slice(0, 4);
}

export function categoryMonthOverMonth(currentTxs, priorTxs) {
  const sumByCategory = (txs) => {
    const map = {};
    (txs || [])
      .filter((t) => t.type === "EXPENSE")
      .forEach((t) => {
        const key = t.category || "OTHER";
        map[key] = (map[key] || 0) + parseFloat(t.amount || 0);
      });
    return map;
  };

  const current = sumByCategory(currentTxs);
  const prior = sumByCategory(priorTxs);
  const keys = new Set([...Object.keys(current), ...Object.keys(prior)]);

  return [...keys]
    .map((cat) => {
      const cur = current[cat] || 0;
      const prev = prior[cat] || 0;
      const delta = cur - prev;
      if (Math.abs(delta) < 0.01 && cur === 0) return null;
      const pct = prev > 0 ? (delta / prev) * 100 : null;
      return { category: cat, current: cur, prior: prev, delta, pct };
    })
    .filter(Boolean)
    .sort((a, b) => Math.abs(b.delta) - Math.abs(a.delta))
    .slice(0, 5);
}

export function billCashflowTimeline(bills, daysAhead = 30) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const horizon = new Date(today);
  horizon.setDate(horizon.getDate() + daysAhead);

  return (bills || [])
    .filter((b) => !b.paid && b.dueDate)
    .map((b) => {
      const raw = typeof b.dueDate === "string" && !b.dueDate.includes("T")
        ? `${b.dueDate}T00:00:00`
        : b.dueDate;
      const due = new Date(raw);
      return { ...b, due };
    })
    .filter((b) => b.due >= today && b.due <= horizon)
    .sort((a, b) => a.due - b.due)
    .slice(0, 6);
}
