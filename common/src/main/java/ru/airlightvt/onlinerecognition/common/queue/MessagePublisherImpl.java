package ru.airlightvt.onlinerecognition.common.queue;

import ru.airlightvt.onlinerecognition.common.queue.model.QueueMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link MessagePublisher} отправителя сообщений в очереди сообщений
 * @author apolyakov
 * @since 23.06.2018
 */
@Service
public class MessagePublisherImpl implements MessagePublisher{
    private RedisTemplate<String, Object> redisTemplate;
    private ChannelTopic topic;

    public MessagePublisherImpl() {
    }

    @Autowired
    public MessagePublisherImpl(@Qualifier("redisJacksonTemplate") RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(QueueMessage message) {
        /*boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<QueueMessage> serializer = (RedisSerializer<QueueMessage>) redisTemplate.getHashValueSerializer();
            connection.set(topic.getTopic().getBytes(), serializer.serialize(message));
            return true;
        });*/
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
