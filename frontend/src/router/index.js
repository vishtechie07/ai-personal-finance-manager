import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("../views/Home.vue"),
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("../views/Register.vue"),
    meta: { guestOnly: true },
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: () => import("../views/Dashboard.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/transactions",
    name: "Transactions",
    component: () => import("../views/Transactions.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/budgets",
    name: "Budgets",
    component: () => import("../views/Budgets.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/bills",
    name: "Bills",
    component: () => import("../views/Bills.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/insights",
    name: "Insights",
    component: () => import("../views/Insights.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/profile",
    redirect: "/settings",
  },
  {
    path: "/settings",
    name: "Settings",
    component: () => import("../views/Settings.vue"),
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

let authColdStart = true;

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    next("/dashboard");
    return;
  }

  if (!to.meta.requiresAuth) {
    next();
    return;
  }
  if (!authStore.token) {
    next({ path: "/", query: { redirect: to.fullPath } });
    return;
  }
  const ok = await authStore.checkAuth({ coldStart: authColdStart });
  authColdStart = false;
  if (!ok) {
    next({ path: "/", query: { redirect: to.fullPath } });
    return;
  }
  next();
});

export default router;
