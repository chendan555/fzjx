/** 后端 REST API 封装 */
async function request(url, opts) {
    const res = await fetch(url, opts)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json()
}

export function connect(form) {
    return request('/api/connect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
    })
}

export function disconnect() {
    return request('/api/disconnect', { method: 'POST' })
}

export function getTables() {
    return request('/api/tables')
}

export function getTopics() {
    return request('/api/topics')
}

export function parseData(table, topics, limit) {
    return request('/api/parse', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ table, topics, limit })
    })
}
