FROM sirlatrom/android:19

ADD . /code
WORKDIR /code
RUN rm -f local.properties

ENV HOME /root
ENV LD_LIBRARY_PATH ${ANDROID_HOME}/tools/lib

# RUN ./gradlew clean lint assemble
CMD export LD_LIBRARY_PATH=${ANDROID_HOME}/tools/lib && echo no | android create avd --force -n test -t android-19 \
    && ./start-emulator "./gradlew connectedAndroidTest"
