# api-gateway

api-gateway that routes client api to backend open api with routing data.

## how it works

when api-gateway server is started, it communicates with rest-api-server(admin server).

it receives all data(routing information).

first, gateway registers mapping with registerMapping methods.

second, gateway stores these data in RoutingDataSet memory repository. (it needs not to communicate with admin server anymore)

when administrator change routing information, then api-gateway updates real-time.

## dto

### ClientRequest

data extracted from ServerHttpRequest (extracted from ServerWebExchange).

convertedBody get from @RequestBody Annotation. 

### DeletedInformation

messages from routing-delete channel.

It just consists of domain+path(originalPath), method to unregisterMapping, delete data in repository.

### HeaderKey

worksKey to use backend openAPI.

### RateLimit

localWindows treats local counts in gateway local memory.

redisWindows treats global counts in redis.

### RoutingData

this data treats routing information from admin server in RoutingDataSet.

### RoutingInformation

RoutingInformation with mongoDB documents.

### UpdatedInformation

messages from routing-update channel.

it consists of previous routingInformation, updatedInformation.

## repository

### RoutingDataSet

gateway gets all routings and stores these data in this repository.

when proxying, these data is utilized and can add, delete.

### RateLimitDataSet

RateLimit data is stored in this repository.

when time goes on, unnecessary data is deleted with time interval.

## scheduler

RedisScheduler is scheduler to synchronize with redis.

local memory (RateLimitDateSet) controllers local count, then setting time(fixedDelay) access redis with these dataSet and update redis.

## rateLimiter

rateLimiter is applied with implementing WebFilter. 

At first, check whether this client's request is registered in api-gateway.

if not, skip.

get ip(XFF headers) and routing's ObjectId. (this can distinguish routing + user in rate limit)

then, update data's timestamp to current time.

and check it is allowed or not in isAllowed function.

