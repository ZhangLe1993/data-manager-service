FROM docker.io/zhangyule1993/java-base:v1.0.0

# RUN npm install -g cnpm --registry=https://registry.npm.taobao.org

ENV active $ACTIVE

COPY deploy.sh /home/deploy.sh

COPY /out/app.jar /home/app.jar

COPY settings.xml /home/settings.xml

WORKDIR /home

CMD ["sh", "deploy.sh"]