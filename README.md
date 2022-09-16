# api-gateway

**```api-gateway```** that routes client api to backend open api with routing data.

use **spring webflux**, **reactive redis**.

## Index

- [Architecture](#Architecture)
- [WorkingFlow](#workingFlow)
- [Controller](#Controller)
- [Dto](#Dto)
- [Error](#Error)
- [Repository](#Repository)
- [Service](#Service)
- [Scheduler](#Scheduler)
- [RateLimiter](#RateLimiter)

## Architecture

<img width="1030" alt="Screen Shot 2022-09-16 at 3 51 25 PM" src="https://user-images.githubusercontent.com/71834320/190575026-3f9a2e39-9c75-4c70-b8c1-1ec3979aba17.png">

## WorkingFlow

<img width="1109" alt="Screen Shot 2022-09-16 at 3 59 48 PM" src="https://user-images.githubusercontent.com/71834320/190576432-e41c216f-7b3c-427e-806b-a033b1fef48e.png">

when api-gateway server is started, it communicates with rest-api-server(admin server).

it receives routing information data stored in mongoDB.

first, gateway **registers** mapping with **```registerMapping```** methods.

second, gateway **stores** these data in **```RoutingDataSet```** memory repository. (it needs not to communicate with admin server anymore)

when administrator change routing information, then api-gateway updates real-time with **```redis pub/sub```**.

## Controller

There are two handler.

first ```route``` handles applied routing information to proxy backend server.

second ```healthCheck``` handles health check with L4 load balancer.

In ```@PostConstruct``` Annotation, communicate with admin server and register mapping, store routing data in memory. 

## Dto

### ```ClientRequest```

data extracted from **ServerHttpRequest** (extracted from **ServerWebExchange**).

convertedBody get from ```@RequestBody``` Annotation. 

### ```DeletedInformation```

messages from routing-delete channel.

It just consists of **domain**+**path**(originalPath), **method** to **```unregisterMapping```**, delete data in repository.

### ```HeaderKey```

worksKey to use **backend openAPI**.

### ```RateLimit```

localWindows treats local counts in gateway **local memory**.

redisWindows treats global counts in **redis**.

### ```RoutingData```

this data treats routing information from admin server in RoutingDataSet.

### ```RoutingInformation```

RoutingInformation with mongoDB documents.

### ```UpdatedInformation```

messages from routing-update channel.

it consists of previous routingInformation, updatedInformation.

## Error

| Property           | Status Code | Error Message          |
|--------------------|-------------|------------------------|
| NO_CONTENT         | 204         | No Content.            |
| BAD_REQUEST        | 400         | 잘못된 형식이나 내용입니다.        |
| INVALID_PARAMETER  | 400         | 잘못된 파라미터 입력입니다.        |
| MISSING_PARAMETER  | 400         | 필수 파라미터를 지정하지 않았습니다.   |
| UNAUTHORIZED       | 401         | 인증되지 않은 클라이언트입니다.      |
| FORBIDDEN          | 403         | 권한이 금지되었습니다.           |
| NOT_FOUND_RESOURCE | 404         | 라우팅 정보 리소스를 찾을 수 없습니다. |
| NOT_FOUND          | 404         | 자원을 찾을 수 없습니다.         |
| TOO_MANY_REQUEST   | 429         | API 사용 요청량을 초과했습니다.    |
| SYSTEM_ERROR       | 500         | 서버 시스템 내부의 오류입니다.      |

## Repository

### ```RoutingDataSet```

gateway gets all routing information and stores these data in this repository.

when proxying, these data is utilized and can add new information, delete unnecessary information.

### ```RateLimitDataSet```

RateLimit data is stored in this repository.

when time goes on, unnecessary data is deleted with time interval.

## Service

### ```GatewayService```

- **routeWithClientAPI**
  - make clientRequest dto and parameter verification, then call backend proxy.
- **registerRouting**
  - addRoutingDataSet + registerRequestMapping
- **addRoutingDataSet**
  - store routing data in RoutingDataSet repository.
- **registerRequestMapping**
  - register mapping info in mappingRegistry field with registerMapping methods.
- **unregisterRouting**
  - deleteRoutingDataSet + unregisterRequestMapping
- **deleteRoutingDataSet**
  - delete routing data from RoutingDataSet repository.
- **unregisterRequestMapping**
  - unregister mapping info from mappingRegistry field with unregisterMapping methods.
- **convertBody**
  - when body string is exists, it converts to Map class.
- **getBodyDataBuffer**
  - to proxy backend server, string body is restored in Flux<DataBuffer> type.

### ```ParameterService```

- **checkVerification**
  - verify parameter (query, path variables, body)
- **checkParameters**
  - checkRequiredSatisfied + checkOptionalSatisfied
- **checkRequiredSatisfied**
  - check required parameter 
- **checkOptionalSatisfied**
  - check optional parameter
- **checkValid**
  - isValidType + validation (range, allowed, length) 
- **isValidType**
  - check parameter type (integer, boolean, string)
- **isValidIntegerValue**
  - validateType = range, check target is in valid range.
- **isValidAllowedValue**
  - validateType = allowed, check target is in array.
- **isValidLengthValue**
  - validateType = length, check target string's length is in valid length.

### ```ProxyService```

- **proxy**
  - get routing data stored in RoutingDataSet, and proxy backend server.
- **getPath**
  - get path used in backend proxying. this change path variable to real value getting from client's request.
  - ex) /boards/{boardId} => /boards/12345

### ```RestApiService```

- **getRoutingInformation**
  - communicate with admin server to get routing information.

## Scheduler

RedisScheduler is scheduler to synchronize with **redis**.

local memory (RateLimitDateSet) controllers local count, then setting time(fixedDelay) access redis with this dataSet and update redis.

### ```Redis Synchronization```

api-gateway is **clustered**. so, each gateway' local memory is synchronized with redis.

To solve Concurrency issues, i use lua script because redis recognize one script as one command.  

**lua script logic**

| Property | Description                      |
|----------|----------------------------------|
| KEYS[1]  | redis key (routing id + user ip) |
| KEYS[2]  | prev local window key            |
| KEYS[3]  | current local window key         |
| ARGV[1]  | can allowed rate Limit counts    |
| ARGV[2]  | prev local window value          |
| ARGV[3]  | current local window value       |

```
1. check prev, current value with redis key + prev local window key / redis key + current local window key.
   (if not exists, initialize to 0)
   
2. count min value between value getting from 1 + value from gateway (ARGV[2], ARGV[3]) and rate Limit (ARGV[1]).

3. store value getting from 2 and set up expire.

4. return updated new value to gateway [prev redis window value, current redis window value]
```

## RateLimiter

rateLimiter is applied with implementing **```WebFilter```**. 

At first, check whether this client's request is registered in api-gateway.

if not, skip.

get ip(**XFF headers**) and routing's ObjectId. (this can distinguish **routing** + **user** in rate limit)

then, update data's timestamp to current time.

and check it is allowed or not in isAllowed function.

**isAllowed function logic**

It should take account of both local count(local Window) and redis count(redis Window).

```
1. get currentWindow key, prevWindow key (prevWindow key = currentWindow key - 1)

2. calculate prev time ratio (1 - (currentTimeMillis - currentWindowKey * 1000) / 1000.0)

3. calculate prev count (min value between rate limit and prev redis value + prev local value)

4. check whether calculated value(prev ratio * prev count + current redis value + current local value) is greater than rate Limit. 

5. if 4 is satisfied, then **```429```** error, return false. else allow request return true.
```