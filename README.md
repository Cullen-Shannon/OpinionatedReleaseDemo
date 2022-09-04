# Examples of Opinionated Releases
Repo supporting Droidcon 2022 NYC talk: *An Opinionated, Gradle-Based Approach to Automated Version Management*  

#### Summary

> Managing your app's release names and numbers is often a manual process, hindering automated CI/CD initiatives. Many teams designate a specific team member to increment the required versionCode/versionName fields, manually push/pull releases branches into downstream branches, and resolve merge conflicts when hotfixing version-related issues.

> With proper Gradle architecture, these pesky versioning tasks can be fully self-deriving, require no manual intervention whatsoever, and support a truly automated CI/CD pipeline.

> This talk will focus on an opinionated approach to addressing these problems, as well as the practical lessons learned after rearchitecting a modern, agile-centric CI/CD pipeline for a big-name app with millions of users.

#### Purpose
Repository to showcase functional examples and implementation surrounding a variety of CI/CD challenges. (The android app itself is merely a placeholder). Examples include: 
- self-deriving version codes
- self-deriving version names
- automated code migration during release cycles
- automated sunsetting of releases
- unit testing methodology
- passing command line arguments to gradle
- kotlin DSL
- convenience method to prune local untracked branches

#### Purpose
Feel free to issue a PR if you've found a better way to handle any of this!