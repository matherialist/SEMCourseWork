FROM gradle

ENV VERSION 0.1.0

ADD . /tmp

WORKDIR /tmp

#RUN apk update && apk add gradle

RUN sed -i 's~"https://hw.dzolotov.tech/api"~"http://127.0.0.1:10080"~ig' src/Application.kt

RUN ./gradlew build -x test

RUN chmod 700 ifmo_rsa

RUN ls -la build

RUN ls -la build/libs

RUN ls -la build/distributions

RUN scp -i ifmo_rsa -o 'StrictHostKeyChecking no' -P 2222 build/libs/admin-0.1.0-all.jar s287701@helios.se.ifmo.ru:/home/s287701

RUN sed -i 's~"http://127.0.0.1:10080"~"https://hw.dzolotov.tech/api"~ig' src/Application.kt

RUN ./gradlew assembleDist -x test

RUN mv build/distributions/*.zip /opt

WORKDIR /opt

RUN unzip admin-0.1.0.zip

RUN mv admin-$VERSION admin

WORKDIR /opt/admin

RUN chmod +x bin/admin

EXPOSE 8080

CMD bin/admin
