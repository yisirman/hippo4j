package cn.hippo4j.springboot.starter.monitor.send.netty;

import cn.hippo4j.common.monitor.Message;
import cn.hippo4j.common.monitor.MessageWrapper;
import cn.hippo4j.common.toolkit.MessageConvert;
import cn.hippo4j.springboot.starter.monitor.send.MessageSender;
import cn.hippo4j.springboot.starter.remote.ServerNettyAgent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Netty ConnectSender
 *
 * @author lk
 * @date 2022/06/18
 */
@Slf4j
@AllArgsConstructor
@Component
public class NettyConnectSender implements MessageSender {

    private ServerNettyAgent serverNettyAgent;

    @Override
    public void send(Message message) {
        MessageWrapper messageWrapper = MessageConvert.convert(message);
        EventLoopGroup eventLoopGroup = serverNettyAgent.getEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE,
                                    ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new SenderHandler(messageWrapper));
                        }
                    });

            bootstrap.connect(serverNettyAgent.getNettyServerAddress(), serverNettyAgent.getNettyServerPort()).sync();
        } catch (Exception e) {
            log.error("netty send error ",e);
        } /*finally {
            eventLoopGroup.shutdownGracefully();
        }*/
    }
}