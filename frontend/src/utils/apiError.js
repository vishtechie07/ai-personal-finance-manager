export function getApiErrorMessage(err, context = {}) {
  if (!err) return "Something went wrong. Please try again.";

  if (err.code === "ECONNABORTED" || err.message?.includes("timeout")) {
    if (context.coldStart) {
      return "The server is waking up (this can take up to a minute on free hosting). Please wait and try again.";
    }
    return "Request timed out. Check your connection and try again.";
  }

  if (!err.response) {
    if (context.coldStart) {
      return "Connecting to SpendSense… first load after idle can take up to a minute.";
    }
    return "Cannot reach the server. Check your connection.";
  }

  const status = err.response.status;
  const data = err.response.data;

  if (data?.error) return data.error;
  if (data?.message) return data.message;

  switch (status) {
    case 400:
      return "Invalid request. Check your input.";
    case 401:
      if (context.auth === "google") {
        return "Google sign-in failed. Try again or use username and password.";
      }
      if (context.auth === "register") {
        return "Could not create your account. Check your details and try again.";
      }
      if (context.auth === "login") {
        return "Invalid username or password.";
      }
      return "Invalid username or password.";
    case 403:
      return "Access denied. You may need to sign in again.";
    case 404:
      return "The requested resource was not found.";
    case 409:
      return "This action conflicts with existing data.";
    case 429:
      return "Too many attempts. Please wait a moment and try again.";
    default:
      return `Request failed (${status}). Please try again.`;
  }
}
