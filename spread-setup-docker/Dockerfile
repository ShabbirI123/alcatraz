FROM gcc:9

RUN set -ex;                                                                      \
    apt-get update;                                                               \
    apt-get install -y cmake libzmq3-dev;                                         \
    mkdir -p /usr/src;                                                            \
    cd /usr/src;                                                                  \
    curl -L https://github.com/zeromq/cppzmq/archive/v4.6.0.tar.gz | tar -zxf -;  \
    cd /usr/src/cppzmq-4.6.0;                                                     \
    cmake -D CPPZMQ_BUILD_TESTS:BOOL=OFF .; make; make install

ADD spread-src /spread-src

RUN /spread-src/configure
RUN make 
RUN make install

COPY ./spread.conf /usr/local/etc/spread.conf

EXPOSE 4803:4803

RUN addgroup --gid 1000 spread
RUN adduser --uid 1000 --gid 1000 --home /home/spread --disabled-password spread
USER spread

CMD ["spread", "-n" ,"localhost"]