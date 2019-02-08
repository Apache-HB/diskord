# Introduction

### So you want to contribute to Strife, eh?

Thank you for even considering it! Writing an implementation of the Discord API is a difficult (and strenuous) task, 
so contributions are welcomed, even if they're simple spelling/grammar corrections. 

### Why are these guidelines important?

Not all code is written equally, but we can try to make it so. Maintaining a consistent code style in the project is important, and the fewer corrections need to be made after the initial pull request, the better.

### What contributions are welcome?

There's a whole laundry list of contributions you can make, but the main ones are:
- Bug reports
- Feature requests
- Documentation
- Samples
- Code!

That said, there are some contributions we wouldn't like. Keep in mind that you shouldn't file an issue for a support question; that should go in our Discord server instead.

# Ground Rules
### Be excellent.

As funny as the pre-rebirth Torvalds quotes are, I'd rather we keep things civil here. Be patient, be welcoming, and 
don't be the one bad experience a contributor to open source may have. I won't give this its own document, nor will I adopt the Contributor Covenant (god forbid), but I feel it's important that I express this.

### Maintain compatibility.

Strife is intended to be a multiplatform project. Keep that in mind when writing code for the project; if it can go in common, it *should* go in common. Put as little code in platform-specific source sets as possible.

### Ask permission.

If there's a feature you really want to see in Strife, and you're willing to write the code for it yourself, pop up a feature request first so we can talk through it. If it sounds good to us, we'll let you know, and you can get to work.

### Adhere to the Kotlin style guide.

Auto-format in IntelliJ does a good job of this by itself, but there are more rules to keep track of than just basic formatting. You can read the style guide [here](https://kotlinlang.org/docs/reference/coding-conventions.html).

### Write documentation for public code.

Is it public? If so, document it! Kdocs aren't necessary for non-public functions/classes/variables, but they are 
absolutely necessary for anything user-facing.
