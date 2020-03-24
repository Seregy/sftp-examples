# File processing example using Spring Integration with SFTP 

## Requirements
 
To test file processing by the application, some running SFTP server is required.

One way to launch it is to get it running with Docker:
```
docker run \               
    -v /Users/username/sftp/upload:/home/foo/upload \
    -p 2222:22 -d atmoz/sftp \
    foo:pass:1001
``` 

This command will start SFTP server with:
 - user `foo` and password `pass`,
 - `2222` local port,
 - volume mounted to `/Users/username/sftp/upload` on your local machine.
Files in this directory will correspond to the `/upload` directory of `foo` user on SFTP server.

Copy some files to the local directory you've mounted and launch the application.

## Configuration

Application can be configured by adjusting the following properties:

| Property name          | Usage                                                                           | Default value |
|------------------------|---------------------------------------------------------------------------------|---------------|
| sftp.host              | SFTP server URL                                                                 | localhost     |
| sftp.port              | SFTP server port                                                                | 2222          |
| sftp.user              | Username for connecting to SFTP server                                          | foo           |
| sftp.password          | Password for connecting to SFTP server                                          | pass          |
| sftp.filePattern       | Regex pattern for files to download from SFTP server                            | *.txt         |
| sftp.poll.periodMs     | Polling period of SFTP server in milliseconds                                   | 5000          |
| sftp.fetch.maxAmount   | Maximum amount of files that can be fetched from SFTP server during one polling | 1             |
| sftp.remote.directory  | Path to remote directory on SFTP server, from which to fetch files              | upload        |
| sftp.local.directory   | Path to local directory, where the files will be transmitted to                 | sftp-inbound  |
| message.queue.capacity | Maximum amount of messages that can be present in message queues                | 5             |