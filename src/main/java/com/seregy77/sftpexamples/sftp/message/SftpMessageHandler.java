package com.seregy77.sftpexamples.sftp.message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;

@Slf4j
@MessageEndpoint
public class SftpMessageHandler {

  /**
   * Process file from SFTP channel and redirects it into local channel
   *
   * @param payload file received from SFTP
   * @return file to be processed by local channel
   */
  @ServiceActivator(inputChannel = "sftpChannel", poller = @Poller(), outputChannel = "localChannel")
  public File receiveFile(File payload) {
    log.info("Processed file: {}", payload.getName());
    return payload;
  }

  /**
   * Remove local file after it was processed
   *
   * @param payload file to remove
   */
  @ServiceActivator(inputChannel = "localChannel")
  public void removeLocalFile(File payload) {
    try {
      Files.delete(payload.toPath());
      log.info("Successfully deleted file: {}", payload.getName());
    } catch (IOException e) {
      log.warn("Couldn't delete file: {}", payload.getName(), e);
    }
  }
}
