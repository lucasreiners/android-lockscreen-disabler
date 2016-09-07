#!groovy

node {
    checkout scm
    env.LD_LIBRARY_PATH = '/var/jenkins_home/tools/android-sdk/tools/lib'
    withCredentials([
      [
       $class        : 'StringBinding',
       credentialsId : '8346e5a7-019d-4c8f-85ed-2cba663293e4',
       variable      : 'KSTOREPWD',
      ],
      [
       $class        : 'StringBinding',
       credentialsId : '8346e5a7-019d-4c8f-85ed-2cba663293e4',
       variable      : 'KEYPWD',
      ],
      [
       $class        : 'FileBinding',
       credentialsId : 'ce24f3f0-efe5-493c-bcad-e02df35a2c09',
       variable      : 'KEYSTORE',
      ],
    ]) {
        gradle clean assembleRelease
    }
}
