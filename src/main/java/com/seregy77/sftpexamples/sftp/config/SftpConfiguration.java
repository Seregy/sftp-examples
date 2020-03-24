package com.seregy77.sftpexamples.sftp.config;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import java.io.File;
import java.util.Arrays;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.scheduling.support.PeriodicTrigger;

@Configuration
@RequiredArgsConstructor
public class SftpConfiguration {

  private final DataSource dataSource;
  @Value("${sftp.host:localhost}")
  private String sftpHost;
  @Value("${sftp.port:2222}")
  private int sftpPort;
  @Value("${sftp.user:foo}")
  private String sftpUser;
  @Value("${sftp.password:pass}")
  private String sftpPass;
  @Value("${sftp.filePattern:*.txt}")
  private String sftpFilePattern;
  @Value("${sftp.remote.directory:upload}")
  private String sftpRemoteDirectory;
  @Value("${sftp.local.directory:sftp-inbound}")
  private String sftpLocalDirectory;

  @Value("${sftp.poll.periodMs:5000}")
  private long sftpPollPeriod;
  @Value("${sftp.fetch.maxAmount:1}")
  private int sftpFetchMaxAmount;
  @Value("${message.queue.capacity:5}")
  private int messageQueueCapacity;

  /**
   * Session factory for creating connections to SFTP server
   *
   * @return sftp session factory
   */
  @Bean
  public SessionFactory<LsEntry> sftpSessionFactory() {
    DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
    factory.setHost(sftpHost);
    factory.setPort(sftpPort);
    factory.setUser(sftpUser);
    factory.setPassword(sftpPass);
    factory.setAllowUnknownKeys(true);
    return new CachingSessionFactory<>(factory);
  }

  /**
   * Message source for producing messages with file payload from SFTP server
   *
   * @return message source
   */
  @Bean
  @InboundChannelAdapter(channel = "sftpChannel", poller = @Poller("sftpPoller"))
  public MessageSource<File> sftpMessageSource() {
    SftpInboundFileSynchronizingMessageSource source =
        new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
    source.setLocalDirectory(new File(sftpLocalDirectory));
    source.setAutoCreateLocalDirectory(true);
    source.setLocalFilter(new AcceptOnceFileListFilter<>());
    source.setMaxFetchSize(sftpFetchMaxAmount);
    return source;
  }

  /**
   * SFTP File synchronizer for fetching new files from SFTP server to local directory
   *
   * @return sftp file synchronizer
   */
  @Bean
  public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
    SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(
        sftpSessionFactory());
    fileSynchronizer.setDeleteRemoteFiles(false);
    fileSynchronizer.setRemoteDirectory(sftpRemoteDirectory);
    CompositeFileListFilter<LsEntry> compositeFileListFilter = new CompositeFileListFilter<>(
        Arrays.asList(new SftpPersistentAcceptOnceFileListFilter(jdbcMetadataStore(), "sftp"),
            new SftpSimplePatternFileListFilter(sftpFilePattern)));
    fileSynchronizer.setFilter(compositeFileListFilter);
    return fileSynchronizer;
  }

  /**
   * Message channel for fetching data from SFTP server
   *
   * @return pollable message channel
   */
  @Bean
  public PollableChannel sftpChannel() {
    return new QueueChannel(messageQueueCapacity);
  }

  /**
   * Message channel local data processing
   *
   * @return pollable message channel
   */
  @Bean
  public MessageChannel localChannel() {
    return new QueueChannel(messageQueueCapacity);
  }


  /**
   * Poller configuration for fetching data from SFTP
   *
   * @return poller config
   */
  @Bean
  public PollerMetadata sftpPoller() {
    PollerMetadata pollerMetadata = new PollerMetadata();
    pollerMetadata.setTrigger(new PeriodicTrigger(sftpPollPeriod));
    return pollerMetadata;
  }

  /**
   * Metadata store for persisting information about already processed files in the database
   *
   * @return jdbc metadata store
   */
  @Bean
  public JdbcMetadataStore jdbcMetadataStore() {
    return new JdbcMetadataStore(dataSource);
  }
}
