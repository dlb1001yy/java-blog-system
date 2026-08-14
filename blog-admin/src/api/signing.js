const SIGNING_SECRET = 'BlogApiSigningSecret2024!'

// Generate a random nonce
function generateNonce() {
  return crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).slice(2) + Date.now().toString(36)
}

// Compute HMAC-SHA256 signature using Web Crypto API
async function computeSignature(method, uri, timestamp, nonce) {
  const stringToSign = [method, uri, timestamp, nonce].join('\n')
  const encoder = new TextEncoder()
  const keyData = encoder.encode(SIGNING_SECRET)
  const key = await crypto.subtle.importKey('raw', keyData, { name: 'HMAC', hash: 'SHA-256' }, false, ['sign'])
  const signature = await crypto.subtle.sign('HMAC', key, encoder.encode(stringToSign))
  // Convert ArrayBuffer to hex string
  return Array.from(new Uint8Array(signature))
    .map(b => b.toString(16).padStart(2, '0'))
    .join('')
}

// Returns { timestamp, nonce, signature } for a request
export async function signRequest(method, url) {
  const timestamp = Date.now().toString()
  const nonce = generateNonce()
  // url comes as relative path like "/admin/links", need to prepend "/api"
  const uri = url.startsWith('/api') ? url : '/api' + url
  const signature = await computeSignature(method.toUpperCase(), uri, timestamp, nonce)
  return { timestamp, nonce, signature }
}
