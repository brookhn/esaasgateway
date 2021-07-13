package com.jiahui.esaasgateway.esaasgateway.config;


import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Component
public class NacosDynamicRouteService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private String dataId="gateway-router";

    private String group = "DEFAULT_GROUP";

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String server_addr;

    @Value("${spring.cloud.nacos.config.namespace}")
    private String cfg_namespace;

    private static List<String> ROUTE_LSIT = new ArrayList<>();

    private Gson gson = new Gson();

    @PostConstruct
    public void dynamicRouteByNacosListener(){
        try {
            Properties properties = new Properties();
            properties.setProperty(PropertyKeyConst.SERVER_ADDR, server_addr);
            properties.setProperty(PropertyKeyConst.NAMESPACE, cfg_namespace);
            ConfigService configService = NacosFactory.createConfigService(properties);

            String confgService = configService.getConfig(dataId, group, 5000);
            System.out.println(confgService);
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    cleanRoute();
                    java.lang.reflect.Type routeDefinitionType = new TypeToken<List<RouteDefinition>>() {}.getType();
                    List<RouteDefinition> gatewayRouteDefinitions = gson.fromJson(configInfo, routeDefinitionType);
                    for (RouteDefinition routeDefinition : gatewayRouteDefinitions) {
                        addRoute(routeDefinition);
                    }
                    publish();
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }


    private void addRoute(RouteDefinition routeDefinition){
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        ROUTE_LSIT.add(routeDefinition.getId());
    }

    private void publish(){
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this.routeDefinitionWriter));
    }

    /**
     *
     */
    private void cleanRoute(){
        for(String id:ROUTE_LSIT)
        {
            this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
        }
        ROUTE_LSIT.clear();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
