FROM gradle

ENV VERSION 0.1.0

ADD . /tmp

WORKDIR /tmp

#RUN apk update && apk add gradle

#Build variant for solaris

RUN sed -i 's/77.222.60.21/127.0.0.1/ig' src/com/holeaf/api/Db.kt

RUN gradle shadowJar -x test

RUN chmod 700 ifmo_rsa

ADD run_api.sh .

RUN chmod +x run_api.sh

RUN scp -i ifmo_rsa -o 'StrictHostKeyChecking no' -P 2222 build/libs/holeaf-api-0.1.0-all.jar s287701@helios.se.ifmo.ru:/home/s287701
RUN scp -i ifmo_rsa -o 'StrictHostKeyChecking no' -P 2222 run_api.sh s287701@helios.se.ifmo.ru:/home/s287701
RUN scp -i ifmo_rsa -o 'StrictHostKeyChecking no' -P 2222 init.sql s287701@helios.se.ifmo.ru:/home/s287701

RUN sed -i 's/127.0.0.1/77.222.60.21/ig' src/com/holeaf/api/Db.kt

RUN gradle assembleDist -x test

RUN mv build/distributions/*.zip /opt

WORKDIR /opt

RUN unzip *.zip

RUN mv holeaf-api-$VERSION api

WORKDIR /opt/api

RUN chmod +x bin/holeaf-api

EXPOSE 8080

CMD bin/holeaf-api
