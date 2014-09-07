FROM sirlatrom/android:19

ADD . /workspace
WORKDIR /workspace

ENV HOME /root
ENV LD_LIBRARY_PATH ${ANDROID_HOME}/tools/lib

ADD start-emulator    /usr/local/bin/
ADD wait-for-emulator /usr/local/bin/
CMD rm -f local.properties && export LD_LIBRARY_PATH=${ANDROID_HOME}/tools/lib && echo no | android create avd --force -n test -t android-19 \
    && start-emulator "./gradlew connectedAndroidTest"
