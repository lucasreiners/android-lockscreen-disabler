FROM sirlatrom/android:19

ADD ./code/ /workspace
WORKDIR /workspace

ENV HOME /root
ENV LD_LIBRARY_PATH ${ANDROID_HOME}/tools/lib

RUN ./gradlew tasks

RUN ./gradlew assembleDebugTest

RUN echo no | android create avd --force -n test -t android-19
ADD start-emulator    /usr/local/bin/
ADD wait-for-emulator /usr/local/bin/
CMD start-emulator "./gradlew connectedAndroidTest"
