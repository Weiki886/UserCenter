-- 基于Redis的令牌桶限流算法
-- 参数:
-- KEYS[1]: Redis键
-- ARGV[1]: 最大令牌数(令牌桶容量)
-- ARGV[2]: 令牌生成速率(每秒生成的令牌数)
-- ARGV[3]: 请求的令牌数
-- ARGV[4]: 当前时间戳(毫秒)
-- ARGV[5]: 令牌桶过期时间(秒)

-- 获取令牌桶当前状态
local bucket = redis.call('hmget', KEYS[1], 'last_tokens', 'last_refreshed')
local last_tokens = tonumber(bucket[1])
local last_refreshed = tonumber(bucket[2])

-- 令牌桶容量和速率
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])
local ttl = tonumber(ARGV[5])

-- 如果令牌桶不存在，则初始化为满状态
if last_tokens == nil or last_refreshed == nil then
    last_tokens = capacity
    last_refreshed = now
end

-- 计算当前令牌数
-- 公式: 上次令牌数 + 生成速率 * 经过时间(毫秒) / 1000
local delta = math.max(0, now - last_refreshed)
local current_tokens = math.min(capacity, last_tokens + (rate * delta / 1000))

-- 判断是否有足够的令牌
local allowed = current_tokens >= requested

-- 更新令牌桶状态
if allowed then
    -- 减去消耗的令牌
    current_tokens = current_tokens - requested
    
    -- 更新令牌桶
    redis.call('hmset', KEYS[1], 'last_tokens', current_tokens, 'last_refreshed', now)
    -- 设置过期时间
    redis.call('expire', KEYS[1], ttl)
    
    return 1 -- 允许请求
else
    -- 无需更新，仅设置过期时间
    redis.call('expire', KEYS[1], ttl)
    
    return 0 -- 拒绝请求
end 