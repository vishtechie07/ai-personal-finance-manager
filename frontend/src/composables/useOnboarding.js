const BANNER_KEY = "spendsense_sample_banner_dismissed";

export function onboardingKey(userId) {
  return userId ? `${BANNER_KEY}_${userId}` : BANNER_KEY;
}

export function isSampleBannerDismissed(userId) {
  return localStorage.getItem(onboardingKey(userId)) === "1";
}

export function dismissSampleBanner(userId) {
  localStorage.setItem(onboardingKey(userId), "1");
}

export function markSampleBannerPending(userId) {
  localStorage.removeItem(onboardingKey(userId));
}
