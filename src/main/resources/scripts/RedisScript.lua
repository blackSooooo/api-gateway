-- KEYS[1] : redis key(API+ip) / KEYS[2] : prevWindowKey / KEYS[3] : currentWindowKey
-- ARGV[1] : rateLimit / ARGV[2] : prevWindowCount / ARGV[3] : currentWindowCount
local prevWindowCount = tonumber(redis.call("zscore", KEYS[1], KEYS[2]))

if prevWindowCount == nil then
    prevWindowCount = 0
end

local currentWindowCount = tonumber(redis.call("zscore", KEYS[1], KEYS[3]))

if currentWindowCount == nil then
    currentWindowCount = 0
end

local updatePrevWindowCount = prevWindowCount
local updateCurrentWindowCount = currentWindowCount

if tonumber(ARGV[2]) ~= 0 then
    updatePrevWindowCount = math.min(updatePrevWindowCount + ARGV[2], ARGV[1])
    redis.call("zadd", KEYS[1], updatePrevWindowCount, KEYS[2])
end

if tonumber(ARGV[3]) ~= 0 then
    updateCurrentWindowCount = math.min(updateCurrentWindowCount + ARGV[3], ARGV[1])
    redis.call("zadd", KEYS[1], updateCurrentWindowCount, KEYS[3])
end

redis.call('expire', KEYS[1], 3)

return { updatePrevWindowCount, updateCurrentWindowCount }