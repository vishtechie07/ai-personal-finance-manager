# SpendSense Design System

Source of truth for Stitch/Figma screens and Vue implementation.

## Platform
- Web, desktop-first (min 1024px dashboard); responsive down to 640px
- Stack: Vue 3, Tailwind CSS, Outfit + Inter

## Color roles
| Role | Token | Hex |
|------|--------|-----|
| Primary | `primary-600` | `#4f46e5` |
| Primary hover | `primary-500` | `#6366f1` |
| Accent | `accent` | `#06b6d4` |
| Surface | `white` / `slate-50` | cards on `#f1f5f9` body |
| Text primary | `slate-900` | headings |
| Text secondary | `slate-600` | labels |
| Text muted | `slate-500` | hints |
| Border | `slate-200` | card borders |
| Success | `success-600` | `#16a34a` |
| Danger | `danger-600` | `#dc2626` |
| Warning | `warning-600` | `#d97706` |

**Do not use** `gray-*` or ad-hoc `blue-*` in new UI — use `slate` + `primary`.

## Typography
- Display / hero values: `text-3xl font-bold tabular-nums`
- KPI values: `text-2xl font-bold tabular-nums`
- Section titles: `text-lg font-semibold text-slate-900`
- Labels: `text-sm font-medium text-slate-600`
- Meta: `text-xs text-slate-500`

## Shape & elevation
- Cards: `rounded-xl border border-slate-200/90 bg-white shadow-sm` (class: `dashboard-card`)
- Buttons primary: `rounded-xl` gradient indigo
- Inputs: `rounded-xl`
- Category pills: `rounded-full text-xs font-medium px-2.5 py-0.5`

## Dashboard IA (v2)
1. Sticky subheader — month picker + primary CTA
2. Hero — net balance + status line
3. KPI row — balance, income, expenses, budget utilization
4. Charts — category donut + 6-month trend
5. Today panel — budget health, bills due, alerts
6. Recent transactions — dense table

## Motion
- Page enter: `animate-slide-up` (subtle, once)
- No hover lift on static cards; hover only on rows and links
