package com.tickatch.reservationservice.reservation.infrastructure.messaging.config;

/**
 * Reservation 서비스 RabbitMQ 설정.
 *
 * <p>Product 서비스에서 발행하는 취소 이벤트를 수신하기 위한 Consumer 설정을 정의한다.
 *
 * <p>구독 큐:
 *
 * <ul>
 *   <li>tickatch.product.cancelled.reservation.queue - 상품 취소 시 예약 처리
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
import io.github.tickatch.common.util.JsonUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Value("${messaging.exchange.product:tickatch.product}")
  private String productExchange;

  /** Product 서비스에서 발행하는 Reservation 취소 이벤트 큐 */
  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION =
      "tickatch.product.cancelled.reservation.queue";

  /** Reservation용 라우팅 키 */
  public static final String ROUTING_KEY_CANCELLED_RESERVATION = "product.cancelled.reservation";

  /** Log 서비스 Exchange (공통 로그용) */
  public static final String LOG_EXCHANGE = "tickatch.log";

  /** Reservation 로그 라우팅 키 */
  public static final String ROUTING_KEY_RESERVATION_LOG = "reservation.log";

  // ========================================
  // Exchange (Consumer도 선언 필요 - 멱등성 보장)
  // ========================================
  // 상품
  @Bean
  public TopicExchange productExchange() {
    return ExchangeBuilder.topicExchange(productExchange).durable(true).build();
  }

  // 로그
  @Bean
  public TopicExchange logExchange() {
    return ExchangeBuilder.topicExchange(LOG_EXCHANGE).durable(true).build();
  }

  // ========================================
  // Queues
  // ========================================
  @Bean
  public Queue productCancelledReservationQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION)
        .withArgument("x-dead-letter-exchange", productExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_CANCELLED_RESERVATION)
        .build();
  }

  // ========================================
  // Bindings
  // ========================================
  @Bean
  public Binding productCancelledReservationBinding(
      Queue productCancelledReservationQueue, TopicExchange productExchange) {
    return BindingBuilder.bind(productCancelledReservationQueue)
        .to(productExchange)
        .with(ROUTING_KEY_CANCELLED_RESERVATION);
  }

  // ========================================
  // Dead Letter Exchange & Queues
  // ========================================
  @Bean
  public TopicExchange deadLetterExchange() {
    return ExchangeBuilder.topicExchange(productExchange + ".dlx").durable(true).build();
  }

  @Bean
  public Queue deadLetterReservationQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION + ".dlq").build();
  }

  @Bean
  public Binding deadLetterReservationBinding(
      Queue deadLetterReservationQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterReservationQueue)
        .to(deadLetterExchange)
        .with("dlq." + ROUTING_KEY_CANCELLED_RESERVATION);
  }

  // ========================================
  // Message Converter & Listener Factory
  // ========================================
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(JsonUtils.getObjectMapper());
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter);
    factory.setDefaultRequeueRejected(false); // 실패 시 DLQ로 이동
    factory.setPrefetchCount(10);
    return factory;
  }
}
