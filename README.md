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