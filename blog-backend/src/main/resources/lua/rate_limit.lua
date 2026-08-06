-- ===================================================================
-- 滑动窗口限流 Lua 脚本
-- 基于 Redis Sorted Set 实现的滑动窗口算法
--
-- KEYS[1]: 限流业务 key (例如 rate_limit:login:127.0.0.1)
-- ARGV[1]: 当前时间戳（毫秒）
-- ARGV[2]: 时间窗口大小（毫秒）
-- ARGV[3]: 时间窗口内允许的最大请求次数
-- ARGV[4]: 当前请求的唯一标识（用于 ZSET 成员去重）
--
-- 返回值：
--   1 = 放行
--   0 = 限流拦截
-- ===================================================================

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local uniqueId = ARGV[4]

-- 1. 移除时间窗口之外的旧请求记录（score <= now - window）
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

-- 2. 统计当前窗口内的请求数
local current = redis.call('ZCARD', key)

-- 3. 判断是否超限
if current >= limit then
    return 0
end

-- 4. 记录当前请求（以当前时间戳为 score，唯一 ID 为 member）
redis.call('ZADD', key, now, uniqueId)

-- 5. 重置 key 的过期时间为一个窗口，避免冷数据残留
redis.call('PEXPIRE', key, window)

return 1
