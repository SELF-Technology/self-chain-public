---
sidebar_label: "Validation Process"
sidebar_position: 8
slug: /technical-docs/PoAI/validation-process
canonical_url: https://docs.self.app/technical-docs/PoAI/validation-process
---

# Validation Process

AI validators participate in every vote to determine the block builder with each new round of block assembly. The AI validator votes independently, without user intervention, in the background. The AI validator's artificial intelligence model is being trained based on the results of previous voting and analysis of the participants' block builders' parameters, as well as the requested manual votes of the AI validator's owner.

* Once the AI validator has voted, it requests user participation in voting at the beginning of its network activity and, using a special algorithm, reduces the number of requests to the wallet owner over time.
* Human participation involves choosing one of two block builder options the AI validator provides. Every user has a different choice of two, and the choices become increasingly shortlisted. The result of the userʼs (wallet ownerʼs) selection is added to the validatorʼs knowledge base and used in the further operation of the AI model.
* In the subsequent period, voting occurs automatically. At a certain point, the AI validator will again send requests for manual voting to replenish its knowledge base and verify that the wallet's owner is a real network user and is active.
* If a wallet owner does not participate in several manual votes in a row, the associated AI validator is questioned, and the AI validator's votes may not be counted in subsequent rounds. This penalty is to encourage ongoing participation in securing the network.
* The order and timing of requests for manual voting are determined randomly to prevent collective voting. An AI validator's owner cannot independently request the possibility of manual voting or influence the timing of its proposal.

Validators admitted to the voting process select the block for the round as illustrated below:

<div>
<img src="/img/PoAI-Block-Selection.png" alt="PoAI Block Selection"/>
</div>

After the voting is completed and the winning block builder is determined, the PoAI mechanism selects one from the active AI validators. The validators additionally double-check the collected block before placing it on the blockchain. The AI validator for color-marker validation of block transactions is selected according to the following parameters:

* The AI validator is active and valid according to the conditions listed above.
* The AI validator did not vote for the winning block builder.

Among the validators who voted, one validator is randomly selected from those who did not vote for the block that won the round. This validator will use [color validation](color-marker-system) to check all transactions in the block.

<div>
<img src="/img/PoAI-Color-Validation.png" alt="PoAI Color Validation"/>
</div>

Based on the formula for calculating the random value, the serial number of the AI validator is determined from the list of voters, which will perform color-marker validation of block transactions. The following image illustrates these concepts.

<div>
<img src="/img/PoAI-Validator-Calculation.png" alt="PoAI Validator Calculation"/>
</div>
