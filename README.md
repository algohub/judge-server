# judge-server
REST server for the judge engine

## Compile


### 1. Install judge-engine

Run `maven install -DskipTests` inside the [judge-engine](https://github.com/algohub/judge-engine) directory.


### 2. Install Redis

Since an local instance of Redis is needed during unit tests, you need to install Redis,

    sudo apt -y install redis-server


### 3. Compile and Run Unit Tests

    ./gradlew build

