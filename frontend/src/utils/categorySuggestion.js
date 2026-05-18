import axios from 'axios'

/** Keyword fallback maps (must align with backend enum names). */
export const TRANSACTION_KEYWORDS = {
  FOOD_GROCERIES: ['food', 'grocery', 'supermarket', 'walmart', 'target', 'costco', 'safeway', 'kroger'],
  FOOD_RESTAURANT: ['restaurant', 'dining', 'meal', 'lunch', 'dinner', 'breakfast', 'coffee', 'snack', 'pizza', 'burger', 'sushi', 'share', 'doordash', 'uber eats'],
  TRANSPORTATION: ['gas', 'fuel', 'uber', 'lyft', 'taxi', 'bus', 'train', 'subway', 'parking', 'toll', 'car', 'vehicle', 'transport'],
  HOUSING_RENT: ['rent', 'lease'],
  HOUSING_UTILITIES: ['utilities', 'electricity', 'water', 'internet', 'cable', 'wifi'],
  HOUSING_MAINTENANCE: ['maintenance', 'repair', 'home', 'house', 'plumbing', 'electrical'],
  ENTERTAINMENT: ['movie', 'theater', 'concert', 'show', 'game', 'ticket', 'amusement', 'park', 'museum', 'zoo', 'entertainment', 'netflix', 'spotify'],
  SHOPPING: ['clothes', 'shoes', 'accessories', 'electronics', 'books', 'gifts', 'shopping', 'store', 'mall', 'online', 'amazon'],
  HEALTHCARE: ['doctor', 'dentist', 'pharmacy', 'medicine', 'medical', 'health', 'insurance', 'hospital', 'clinic', 'therapy'],
  SALARY: ['salary', 'wage', 'income', 'payment', 'deposit', 'bonus', 'commission', 'overtime', 'paycheck'],
  INVESTMENTS: ['dividend', 'interest', 'investment', 'stock', 'bond', 'fund', 'portfolio', 'return', 'profit']
}

export const BUDGET_KEYWORDS = {
  FOOD_GROCERIES: ['food', 'groceries', 'supermarket', 'walmart', 'target', 'costco'],
  FOOD_RESTAURANT: ['dining', 'restaurant', 'meal', 'lunch', 'dinner', 'breakfast', 'cafe', 'coffee', 'snack'],
  TRANSPORTATION: ['transport', 'gas', 'fuel', 'car', 'uber', 'lyft', 'taxi', 'bus', 'train', 'subway', 'parking', 'maintenance', 'insurance'],
  HOUSING_RENT: ['rent', 'lease'],
  HOUSING_UTILITIES: ['utilities', 'electricity', 'water', 'internet', 'wifi'],
  HOUSING_MAINTENANCE: ['maintenance', 'repair', 'furniture'],
  ENTERTAINMENT: ['entertainment', 'movie', 'game', 'concert', 'show', 'theater', 'sport', 'hobby', 'gym', 'fitness', 'streaming'],
  SHOPPING: ['shopping', 'clothes', 'electronics', 'books', 'gift', 'fashion', 'accessories', 'beauty', 'cosmetics'],
  HEALTHCARE: ['health', 'medical', 'doctor', 'dentist', 'pharmacy', 'medicine', 'insurance', 'therapy', 'wellness'],
  EDUCATION: ['school', 'college', 'university', 'course', 'training', 'education', 'tuition', 'books', 'supplies'],
  TRAVEL: ['vacation', 'trip', 'flight', 'hotel', 'travel', 'tourism', 'lodging', 'airfare']
}

function keywordBestMatch(description, keywordMap) {
  const descriptionLower = description.toLowerCase()
  let bestMatch = null
  let bestScore = 0
  for (const [category, keywords] of Object.entries(keywordMap)) {
    let score = 0
    for (const keyword of keywords) {
      if (descriptionLower.includes(keyword)) score += 1
    }
    if (score > bestScore) {
      bestScore = score
      bestMatch = category
    }
  }
  return bestScore > 0 ? bestMatch : null
}

/**
 * Uses OpenAI via backend (user key or platform key on Render); otherwise keyword fallback.
 */
export async function resolveCategory(description, keywordMap) {
  if (!description || description.trim().length < 3) return null
  try {
    const { data } = await axios.post(
      '/ai/suggest-category',
      { description: description.trim() },
      { timeout: 45000 }
    )
    if (data?.category) return data.category
  } catch (_) {
    /* network / timeout — fall back */
  }
  return keywordBestMatch(description, keywordMap)
}
