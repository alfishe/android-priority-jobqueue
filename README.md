![logo](http://downloads.path.com/logo.png)

Android Priority Job Queue (Job Manager)
==========================

Priority Job Queue is an implementation of a [Job Queue](http://en.wikipedia.org/wiki/Job_queue) specifically written for Android to easily schedule jobs (tasks) that run in the background, improving UX and application stability.

It is written primarily with [flexibility][10] & [functionality][11] in mind. This is an ongoing project, which we will continue to add stability and performance improvements.

  - [Why ?](#why-)
  - [Show me the code](#show-me-the-code)
  - [What's happening under the hood?](#under-the-hood)
  - [Advantages](#advantages)
  - [Getting Started](#getting-started)
  - [Building](#building)
   - [Running Tests](#running-tests)
  - [wiki][9]
  - [License](#license)


### Why ?
Great client applications cache as much data as possible to provide great user experiences, especially during spotty network connectivity. As users use the app, the UI updates instantly, silently syncing changes with the server.
Since your app uses the internet, you create a slew of resource-heavy operations (web requests, string parsing, database queries, etc) that fight for network bandwidth and CPU time on the device. As you build more features, it can become difficult to schedule and prioritize these tasks. This is where Job Manager comes to the rescue.

Job Queue was inspired by a [Google I/O 2010 talk on REST client applications][8].
Although not required, it is most useful when used with an event bus and a dependency injection framework.

### Show me the code

Since a code example is worth thousands of documentation pages, here it is. ([full version](https://github.com/path/android-priority-jobqueue/wiki/complete-job-example))

File: SendTweetJob.java
``` java
// A job to send a tweet
public class PostTweetJob extends BaseJob implements Serializeable {
    private String text;
    public PostTweetJob(String text) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits while this job is running
        super(true, true);
    }
    @Override
    public void onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job is about to run.
        // In this example, it would be good to update the UI with the newly posted tweet.
    }
    @Override
    public void onRun() throws Throwable {
        // Job logic goes here. In this example, the network call to post to Twitter is done here.
        webservice.postTweet(text);
    }
    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        // An error occurred in onRun.
        // Return value determines whether this job should retry running (true) or abort (false).
    }
    @Override
    protected void onCancel() {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
    }
}


```

File: TweetActivity.java
``` java
//...
public void onSendClick() {
    final String status = editText.getText();
    editText.setText("");
    // Since JobManager.addJob() accesses the disk, we use an AsyncTask to perform the call
    new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            jobManager.addJob(1, new PostTweetJob(status)); 
        }
    }.execute();
}
...
```


That's it. :) Job Manager allows you to enjoy:

* No network calls in activity-bound async tasks
* No serialization mess for important requests
* No "manual" implementation of network connectivity checks or retry logic

### Under the hood
* When user clicked the send button, `onSendClick()` was called, which creates a `PostTweetJob` and adds it to Job Queue for execution.
It runs on a background thread because Job Queue will make a disk access to persist the job.

* Right after `PostTweetJob` is synchronized to disk, Job Queue calls `DependencyInjector` (if provided) which will [inject fields](http://en.wikipedia.org/wiki/Dependency_injection) into our job instance. 
At `PostTweetJob.onAdded()` callback, we saved `PostTweetJob` to disk. Since there has been no network access up to this point, the time between clicking the send button and reaching `onAdded()` is within fracions of a second. This allows the implementation of `onAdded()` to display the newly sent tweet almost instantly, creating a "fast" user experience.

* When it's time for `PostTweetJob` to run, Job Queue will call `onRun()` (and it will only be called if there is an active network connection, as dictated at the job's constructor). 
By default, Job Queue uses a simple connection utility that checks `ConnectivityManager` (ensure you have `ACCESS_NETWORK_STATE` permission in your manifest). You can provide a [custom implementation][1] which can
add additional checks (e.g. your server stability). You should also provide a [`NetworkUtil`][1] which can notify Job Queue when network
is recovered so that Job Queue will avoid a busy loop and decrease # of consumers(default configuration does it for you). 

* Job Queue will keep calling `onRun()` until it succeeds (or reaches a retry limit). If `onRun()` throws an exception,
Job Queue will call `shouldReRunOnThrowable()` to allow you to handle the exception and decide whether to retry job execution or abort.

* If all retry attempts fail (or when `shouldReRunOnThrowable()` returns false), Job Queue will call `onCancel()` to allow you to clean
your database, inform the user, etc.

### Advantages
* It is very easy to de-couple application logic from your activites, making your code more robust, easy to refactor, and easy to **test**.
* You don't have to deal with `AsyncTask` lifecycles. This is true assuming you use an event bus to update your UI (you should).
At Path, we use [GreenRobot's Eventbus](github.com/greenrobot/EventBus); however, you can also go with your favorite. (e.g. [Square's Otto] (https://github.com/square/otto))
* Job Queue takes care of prioritizing jobs, checking network connection, running them in parallel, etc. Job prioritization is especially indispensable when you have a resource-heavy app like ours.
* You can delay jobs. This is helpful in cases like sending a GCM token to your server. It is very common to acquire a GCM token and send it to your server when a user logs in to your app, but you don't want it to interfere with critical network operations (e.g. fetching user-facing content).
* You can group jobs to ensure their serial execution, if necessary. For example, assume you have a messaging client and your user sent a bunch of messages when their phone had no network coverage. When creating these `SendMessageToNetwork` jobs, you can group them by conversation ID. Through this approach, messages in the same conversation will send in the order they were enqueued, while messages between different conversations are still sent in parallel. This lets you effortlessly maximize network utilization and ensure data integrity.
* By default, Job Queue monitors network connectivity (so you don't need to worry about it). When a device is operating offline, jobs that require the network won't run until connectivity is restored. You can even provide a custom [`NetworkUtil`][1] if you need custom logic (e.g. you can create another instance of Job Queue which runs only if there is a wireless connection).
* It is unit tested and mostly documented. You can check our [code coverage report][3] and [Javadoc][4].


### Getting Started
* [Download latest jar][5]
* [Configure job manager][10]
* [Configure individual jobs][11]
* [Review sample app][6]
* [Review sample configuration][7]

### [Wiki][9]

### Building
* Checkout the repo
* `> cd jobqueue`
* `> ant clean build-jar`
This will create a jar file under _release_ folder.

#### Running Tests
* > `cd jobqueue`
* > `ant clean test`


## License

Android Priority Jobqueue is made available under the [MIT license](http://opensource.org/licenses/MIT):

<pre>
The MIT License (MIT)

Copyright (c) 2013 Path, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
</pre>


[1]: https://github.com/path/android-priority-jobqueue/blob/master/jobqueue/src/com/path/android/jobqueue/network/NetworkUtil.java
[2]: https://github.com/path/android-priority-jobqueue/blob/master/jobqueue/src/com/path/android/jobqueue/network/NetworkEventProvider.java
[3]: http://path.github.io/android-priority-jobqueue/coverage-report/index.html
[4]: http://path.github.io/android-priority-jobqueue/javadoc/index.html
[5]: https://github.com/path/android-priority-jobqueue/releases
[6]: https://github.com/path/android-priority-jobqueue/tree/master/examples
[7]: https://github.com/path/android-priority-jobqueue/blob/master/examples/twitter/TwitterClient/src/com/path/android/jobqueue/examples/twitter/TwitterApplication.java#L26
[8]: http://www.youtube.com/watch?v=xHXn3Kg2IQE
[9]: https://github.com/path/android-priority-jobqueue/wiki
[10]: https://github.com/path/android-priority-jobqueue/wiki/Job-Manager-Configuration
[11]: https://github.com/path/android-priority-jobqueue/wiki/Job-Configuration