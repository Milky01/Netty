package fun.xmilk.nio_3_LineBasedFrameDecoder.server;

import fun.xmilk.nio_3_LineBasedFrameDecoder.server.handler.TimeServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class TimeServer {

    public void bind(int port) {
        //接受客户端连接
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        //SocketChannel网络读写
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //用于启动NIO服务端的辅助启动类 降低服务端的开发难度
            ServerBootstrap b = new ServerBootstrap();
            b.group(boosGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());
            //绑定端口,同步等待成功
            ChannelFuture f = b.bind(port).sync();

            //等待服务端监听接口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放资源
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class  ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //注意顺序
            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            socketChannel.pipeline().addLast(new StringDecoder());
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) {
        int port = 60010;
        if (args != null && args.length > 0){
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e){
                port = 60010;
            }
        }
        new TimeServer().bind(port);
    }
}
