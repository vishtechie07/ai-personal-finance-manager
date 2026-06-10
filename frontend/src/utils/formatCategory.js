const LABELS = {
  FOOD_GROCERIES: "Groceries",
  FOOD_RESTAURANT: "Dining",
  TRANSPORTATION: "Transport",
  HOUSING_RENT: "Rent",
  HOUSING_UTILITIES: "Utilities",
  HOUSING_MAINTENANCE: "Maintenance",
  HEALTHCARE: "Healthcare",
  ENTERTAINMENT: "Entertainment",
  SHOPPING: "Shopping",
  EDUCATION: "Education",
  TRAVEL: "Travel",
  INSURANCE: "Insurance",
  INVESTMENTS: "Investments",
  SALARY: "Salary",
  FREELANCE: "Freelance",
  BUSINESS: "Business",
  OTHER: "Other",
};

export function formatCategoryLabel(category) {
  if (!category) return "Other";
  return LABELS[category] || category.replace(/_/g, " ").toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
}

export function formatMoney(value) {
  const n = Number(value);
  if (Number.isNaN(n)) return "$0.00";
  return n.toLocaleString("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  });
}
