# Contributing

Thank you for contributing to the KubeVirt Cloud plugin.

## Where to contribute

- **Bug reports and feature requests:** [GitHub Issues](https://github.com/jenkinsci/kubevirt-cloud-plugin/issues)
- **Source code and documentation changes:** pull requests against the `main` branch

Please do not use the issue tracker for general usage questions. For help running Jenkins or this plugin, use the [Jenkins community](https://www.jenkins.io/participate/connect/).

## Development setup

Requirements: JDK 21 (and optionally 25 for local parity with CI), Maven 3.9+.

```bash
git clone https://github.com/jenkinsci/kubevirt-cloud-plugin.git
cd kubevirt-cloud-plugin
mvn verify
```

The plugin HPI is built at `target/kubevirt-cloud.hpi`.

## Pull requests

1. Fork the repository (or push a branch if you are a maintainer).
2. Make focused changes; keep unrelated refactoring out of the same PR when possible.
3. Add or update tests when behavior changes.
4. Ensure `mvn verify` passes locally.
5. Open a PR against `main`. The **Jenkins** check from [ci.jenkins.io](https://ci.jenkins.io/job/Plugins/job/kubevirt-cloud-plugin/) must pass before merge.

Label your PR when appropriate (`enhancement`, `bug`, `developer`, `chore`, etc.) so release notes and continuous delivery behave as expected. See the [Jenkins CD documentation](https://www.jenkins.io/doc/developer/publishing/releasing-cd/).

## Licensing

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE) already used by this project.

## Further reading

- [Jenkins plugin development](https://www.jenkins.io/doc/developer/plugin-development/)
- [Plugin documentation guide](https://www.jenkins.io/doc/developer/publishing/documentation/)