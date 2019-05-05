<img src="http://www.wring.io/images/logo.svg" height="92" width="92"/>

[![EO principles respected here](http://www.elegantobjects.org/badge.svg)](http://www.elegantobjects.org)
[![Managed by Zerocracy](https://www.0crat.com/badge/C7FCB1EQN.svg)](https://www.0crat.com/p/C7FCB1EQN)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/wring)](http://www.rultor.com/p/yegor256/wring)
[![We recommend IntelliJ IDEA](http://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Build Status](https://travis-ci.org/yegor256/wring.svg?branch=master)](https://travis-ci.org/yegor256/wring)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/wring)](http://www.0pdd.com/p?name=teamed/yegor256/wring)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/wring)](https://hitsofcode.com/view/github/yegor256/wring)

[Wring.io](http://www.wring.io) is a fully automated collector of
events you are getting from GitHub and some other systems where you
actively participate in discussions. Instead of regularly checking
your email and finding what's relevant to you, you configure Wring
to pull those events for you and then you read them in one simple
web page.

It is free.

Read about it at this blog post:
[_Wring.io, a Dispatcher of GitHub Notifications_](http://www.yegor256.com/2016/03/15/wring-dispatcher-github-notifications.html)

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.2+ and Java 8+.

