export function getApiErrorMessage(err) {
  if (!err) return 'Something went wrong. Please try again.'

  if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
    return 'Request timed out. Check your connection and try again.'
  }

  if (!err.response) {
    return 'Cannot reach the server. Check your connection.'
  }

  const status = err.response.status
  const data = err.response.data

  if (data?.error) return data.error
  if (data?.message) return data.message

  switch (status) {
    case 400:
      return 'Invalid request. Check your input.'
    case 401:
      return 'Invalid username or password.'
    case 403:
      return 'Access denied. You may need to sign in again.'
    case 404:
      return 'The requested resource was not found.'
    case 409:
      return 'This action conflicts with existing data.'
    default:
      return `Request failed (${status}). Please try again.`
  }
}
