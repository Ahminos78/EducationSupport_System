@echo off
set DB_HOST=127.0.0.1
set DB_USERNAME=root
set DB_PASSWORD=123456
set DB_NAME=edu_platform
set EDU_JWT_SECRET=edu-platform-dev-secret-change-me
rem set EDU_AI_DEEPSEEK_API_KEY=your_api_key_here

set BASE=D:\ChineseSoftWork\realwork\EducationSupport_System\edu-backend
set LOG=%BASE%\logs

start "gateway" /min java -jar %BASE%\edu-gateway\target\edu-gateway-0.0.1-SNAPSHOT.jar > %LOG%\gateway.log 2>&1
start "user" /min java -jar %BASE%\edu-user-service\target\edu-user-service-0.0.1-SNAPSHOT.jar > %LOG%\user.log 2>&1
start "course" /min java -jar %BASE%\edu-course-service\target\edu-course-service-0.0.1-SNAPSHOT.jar > %LOG%\course.log 2>&1
start "enrollment" /min java -jar %BASE%\edu-enrollment-service\target\edu-enrollment-service-0.0.1-SNAPSHOT.jar > %LOG%\enrollment.log 2>&1
start "assessment" /min java -jar %BASE%\edu-assessment-service\target\edu-assessment-service-0.0.1-SNAPSHOT.jar > %LOG%\assessment.log 2>&1
start "ai" /min java -jar %BASE%\edu-ai-service\target\edu-ai-service-0.0.1-SNAPSHOT.jar > %LOG%\ai.log 2>&1

echo All 6 services started.
