[//]: # (Many of these have been directly quoted from the atom contributing guide https://github.com/atom/atom/blob/master/CONTRIBUTING.md)
# Contributing

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

The following is a set of guidelines for contributing to GRIP.

## What should I know before I get started?

### Code of Conduct

This project adheres to the [Contributor Covenant 1.3](http://contributor-covenant.org/version/1/3/0/).
By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

This section guides you through submitting a bug report for GRIP. Following these guidelines helps maintainers and the community understand your report :pencil:, reproduce the behavior :computer: :computer:, and find related reports :mag_right:.

Before creating bug reports, please check [this list](#before-submitting-a-bug-report) as you might find out that you don't need to create one. When you are creating a bug report, please [include as many details as possible](#how-do-i-submit-a-good-bug-report).

#### Before Submitting A Bug Report

* **Perform a [cursory search](https://github.com/WPIRoboticsProjects/GRIP/issues?utf8=%E2%9C%93&q=)** to see if the problem has already been reported. If it has, add a comment to the existing issue instead of opening a new one.

#### How Do I Submit A (Good) Bug Report?

Explain the problem and include additional details to help maintainers reproduce the problem:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible. For example, start by explaining how you started creating your pipeline in GRIP. When listing steps, **don't just say what you did, but explain how you did it**.
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include screenshots and animated GIFs** which show you following the described steps and clearly demonstrate the problem.
* **If you're reporting that GRIP crashed**, include a crash report with a stack trace from the program and, if possible the operating system.
* **If the problem is related to performance**, please provide more details about what became slow or irresponsive.
* **If the problem wasn't triggered by a specific action**, describe what you were doing before the problem happened and share more information using the guidelines below.

Provide more context by answering these questions:

* **Can you reliably reproduce the issue?** If not, provide details about how often the problem happens and under which conditions it normally happens.
* If the problem is related to working a source, **does the problem happen for all sources or only some** (eg. only with webcams)?

Include details about your configuration and environment:</br>
(If GRIP generated an exception alert you can copy this directly from this dialog)

* **Which version of GRIP are you using?** This will be the version number on the installer you downloaded. (In the future this will be in the crash log).
* **What's the name and version of the OS you're using**?
* **Are you running GRIP in a virtual machine?** If so, which VM software are you using and which operating systems and versions are used for the host and the guest?

### Pull Requests (PR)

* Include screenshots and animated GIFs in your pull request whenever possible.
* Explain what it does, why it is useful, what it fixes, why it should be merged.
* Use `java.util.Optional` instead of `null` for object member variables that may not be defined.
* Project owners have the final say on whether a PR is approved.

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Try to limit the first line to 72 characters if possible
* Reference issues and pull requests liberally

### Documentation Styleguide

* Use JavaDocs

### Code Formatting

* GRIP uses standard Java formatting rules, although we don't care if your line length is over 80 characters. You can probably use your IDE's built-in formatter. This helps keep the code more consistent and readable.
