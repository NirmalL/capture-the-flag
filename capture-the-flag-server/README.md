Capture the Flag server app
===========================

Implementation is done using the socket.io library for node.js. 
The application can be easily uploaded to Heroku service.

For more information about socket.io library visit http://www.socket.io

Installation instructions
--------------------------------------------------------------------------------

**Local setup**

If you just want to use the server in `localhost`:

1. Install node.js from http://www.nodejs.org
2. Open command prompt (Windows users open 'Nodejs command prompt')
3. Run `npm install` this will install required node modules
4. Run `node server.js` to start the server

The server is started at port 8080.

**Installing and running in Heroku**

1. Sign up to Heroku (https://www.heroku.com/)
2. Install Heroku Toolbelt (https://toolbelt.heroku.com/)
3. Go to the server folder and init a git repository with command `git init`
4. Run `git add .`
5. Run `git commit -m "Ready to deploy"`
5. Then we need to create heroku server so run `heroku create`
6. Enable websockets by running `heroku labs:enable websockets`
7. Upload server to heroku by running `git push heroku master` (you should see
   heroku installing all the required dependencies for the server)
8. Run `heroku open` and it will open a web browser that points to the server
   URL, you should now see "Capture the Flag server" in the browser window

If you make changes to the code and want to update the changes to Heroku, just
do a normal git commit and push changes with `git push heroku master` and it
will update the server to use new changes.


**Installing and running in Cloud9**

1. Sign up to Cloud9 (https://c9.io/)
2. Create a project and open the workspace
2. Upload the server software files to your workspace
3. Open the terminal tab
4. Run command `npm install`
5. Open `server.js`
6. Click **Run** button
