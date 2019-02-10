# Introduction

### So you want to contribute to Strife, eh?

Thank you for even considering it! Writing an implementation of the Discord API is a difficult (and strenuous) task, so contributions are welcomed, even if they're simple spelling/grammar corrections.

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

As funny as the pre-rebirth Torvalds quotes are, I'd rather we keep things civil here. Be patient, be welcoming, and don't be the one bad experience a contributor to open source may have. I won't give this its own document, nor will I adopt the Contributor Covenant (god forbid), but I feel it's important that I express this.

### Keep it focused.

If you're going to contribute, set your sights on one feature at a time; ideally, one feature per PR. Keep your changes as contained as possible. I know it’s hard to not immediately fix a given piece of code or formatting when you see it (trust me, I get it, I do the same thing), but it makes the process simpler for everyone involved.

### Maintain compatibility.

Strife is intended to be multiplatform. Keep that in mind when writing code for the project; if it can go in common, it *should* go in common. Put as little code in platform-specific source sets as possible. Ideally, *nothing* would go in platform source sets, but there will always be exceptions.

### Ask permission.

If there's a feature you really want to see in Strife, and you're willing to write the code for it yourself, pop up a feature request issue first so we can talk through it. If it sounds good to us, we'll let you know, and you can get to work.

### Adhere to the Kotlin style guide.

Auto-format in IntelliJ only goes so far. The Kotlin style guide goes over class and file layout, package structure, naming rules, redundancy, documentation formatting, and more. You can (*and should*) read the style guide [here](https://kotlinlang.org/docs/reference/coding-conventions.html).

### Write documentation for public code.

Is it public? If so, document it! Kdocs aren't necessary for non-public functions/classes/variables, but they are absolutely necessary for anything user-facing. Remember, good code is self-documenting in that it should be apparent *what* the code does, but internal documentation is good for when you need to explain *why* the code does what it does. Public documentation should explain what the code does, though. You can read more about Kotlin's KDoc format [here](https://kotlinlang.org/docs/reference/kotlin-doc.html).
