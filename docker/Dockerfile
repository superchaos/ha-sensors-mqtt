#基础镜像，如果本地没有，会从远程仓库拉取。
FROM openjdk:11

#镜像的制作人
MAINTAINER superchao/superchaos@163.com

##这里的 /tmp 目录就会在运行时自动挂载为匿名卷，任何向 /tmp 中写入的信息都不会记录进容器存储层，可以多个
VOLUME ["/tmp"]

# 将jar包添加到容器中并更名为app.jar
ADD *.jar app.jar

#指定容器启动时要执行的命令，但如果存在CMD指令，CMD中的参数会被附加到ENTRYPOINT指令的后面
ENTRYPOINT ["java","-jar","/app.jar"]