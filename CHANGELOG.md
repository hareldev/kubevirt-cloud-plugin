# Changelog

Notable changes to this project are listed here and in [GitHub Releases](https://github.com/jenkinsci/kubevirt-cloud-plugin/releases).

Release versioning follows [Jenkins plugin CD](https://www.jenkins.io/doc/developer/publishing/releasing-cd/) (`<commit-count>.v<git-hash>`), not semantic versioning.

## [33.vc2cc6a_f7a_96d](https://github.com/jenkinsci/kubevirt-cloud-plugin/releases/tag/33.vc2cc6a_f7a_96d) — 2026-11-26

- Configurable **storage class** for VM disk DataVolumes (ContainerDisk, DataSource clone, and DataSource import); optional per template, available in the UI and Jenkins Configuration as Code (`storageClass`).

## [23.vcc5c656b_b_fa_6](https://github.com/jenkinsci/kubevirt-cloud-plugin/releases/tag/23.vcc5c656b_b_fa_6) — 2026-11-25

- First release published to the Jenkins Update Center.
- Dynamic Jenkins agents as VMs on KubeVirt / OpenShift Virtualization.