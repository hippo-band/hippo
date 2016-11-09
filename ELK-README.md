#ELK是ElasticSearch+logstash+kibana三个工具的简称,这是搭载日志分析平台的主要技术框架

##ElasticSearch
      ElasticSearch现在已经是日渐流行的搜索框架，基于lucene，天生具有分布式能力，所以能够给搜索更强有力的支持，在ELK里作为logsatsh的日志输出存储    库，kibana通过抽取es的数据进行分析展示。
      1.启动方式
          官网下载ElasticSearch,最好是高版本，不然会出现一些奇怪的问题，提供两种方式启动
          1.1  ./bin/elasticSearch 直接启动，不过终端一关掉，就会停止
          1.2  nohup ./bin/elasticSearch& 会让elasticSearch 后台启动
      2.使用方式
           在ELK中,elasticSearch作为存储源，只需要对外提供自身的地址和端口即可，其他的都由logstash和kibana来处理
##kibana
     Kibana是一个基于浏览器页面的Elasticsearch前端展示工具,提供了非常牛逼的图表和表现能力。Kibana全部使用HTML语言和Javascript编写的.因此可以部    署到任意的Web容器中去.当然,官网上下载的安装包中已经内置了一个Web容器,直接运行即可.
      1.启动方式
          与ElasticSearch类似
          1.1 ./bin/kibana  直接前台启动
          1.2 nohup ./bin/kibana后台启动
          
      2.使用方式
         kibana需要结合ElasticSearch来可以使用，所以在kibana的配置文件/config/kibana.yml需要指定ElasticSearch的地址，
                     添加： elasticsearch.url: "http://localhost:9200"（你的ElasticSearch地址）
                     
      3.查询语法
         kibana的语法都是基于lucene的语法，详细语法可参考：https://segmentfault.com/a/1190000002972420
         
##logstash
       logstash是一个数据分析软件，主要目的是分析log日志。 整一套软件可以当作一个MVC模型，logstash是controller层，Elasticsearch是一个model层       kibana是view层
      1.启动方式
          logstash需要带配置文件一起启动
          1.1 ./bin/logstash -f logstash.conf
          1.2 nohup ./bin/logstahs -f logstash.conf&
      2.使用方式
          logstash的配置文件需要三个部分， Input,Filter,Output，这三个部分，logstash提供了很多的插件来支持
          2.1 Input  输入部分，标明的是数据来源，可以从文件，命令行输入，redis输入，例如redis插件
                 input {
                     redis {
                         codec => json
                         host => "10.25.9.69"
                         port => 6380
                         password =>"Redis@123"
                         timeout =>10000
                         key => "logstash"
                         data_type => "list"
                         } #输入
                  }
              redis插件是业务系统将日志喷到redis的日志当中，然后logstash再从redis的队列中拿数据，key就是redis的key
          2.2 Filter  过滤部分 这个部分的功能将非结构化的数据转化成自己所需要的机构类型，举例：
                 filter{
                       grok {
                            match => {
                                "message" => "[\s\S]+requestId[:=]'(?<requestId>[\s\S]+?)'[\s\S]"
 
                                      }
                          }}#过滤器
                 这是Filter的grok插件，将符合表达式的内容转换成自己想要的内容
          2.3 Output  输出部分，将过滤后的数据     输出到存储源，也就是输入到我们的ElasticSearch,举例：
                 output {            #输出
                           elasticsearch { hosts => "http://localhost:9200" }
                         }
                
           
                                           
##业务系统整合ELK
    业务系统与ELK的整合与业务选择的日志记录方式的不同而不同，这里主要介绍两种模式，一种是logback 还有一种是log4j

###logback
      使用logback的业务，我采用redis的模式与logstash对接，需要在logback的配置文件logback.xml里面添加如下内容
         <appender name="LOGSTASH" class="com.cwbase.logback.RedisAppender">
		       <source>cloud.igoldenbeta.mercurius.sync.service</source>
	        	<type>类型</type>
		        <tag>标签</tag>
		        <host>redis地址</host>
	        	<port>redis端口</port>
	        	<key>redis的keylogstash</key>
		        <password>redis密码</password>
		        <timeout>redis连接超时时间</timeout>
	       </appender>
         <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
            <appender-ref ref="LOGSTASH" />
         </appender>
         <root level="INFO">
            <appender-ref ref="LOGSTASH" />
            <appender-ref ref="ASYNC" />
         </root>
      当然这两个类，你需要依赖进来
               com.cwbase.logback.RedisAppender
               ch.qos.logback.classic.AsyncAppender
      logstash配置如上文介绍的就可以
           
###log4j
    log4j 采用tcp的模式 
      logstash 的input需要这么配置：
                         input {
                            log4j {
                              mode => server
                              host => "0.0.0.0"
                              port => [log4j_port]
                              type => "log4j"
                            }
                          }
    
     log4j.properties 需要这么配置:
                          log4j.rootLogger=[myAppender]
                          log4j.appender.[myAppender]=org.apache.log4j.net.SocketAppender
                          log4j.appender.[myAppender].port=[log4j_port]
                          log4j.appender.[myAppender].remoteHost=[logstash_host]
    


       
    
          
