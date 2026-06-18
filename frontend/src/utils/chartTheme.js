/** Shared Chart.js typography — matches Outfit in index.html / main.css */
export const CHART_FONT_FAMILY = "'Outfit', 'Inter', system-ui, sans-serif";

export const chartFont = (size = 12, weight = "500") => ({
  family: CHART_FONT_FAMILY,
  size,
  weight,
});

export function doughnutTooltipLabel(context, formattedAmount) {
  const total = context.dataset.data.reduce((a, b) => a + b, 0);
  const value = context.parsed;
  const pct = total > 0 ? ((value / total) * 100).toFixed(1) : "0";
  return `${context.label}: ${formattedAmount(value)} (${pct}%)`;
}
