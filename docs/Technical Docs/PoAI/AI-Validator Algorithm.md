---
sidebar_label: "AI-Validator Algorithm"
sidebar_position: 6
---

# AI-Validator Algorithm

AI validators are lite blockchain nodes that store network wallet addresses and color markers. [Color markers](Color%20Marker%20System) are a key part of validation and connecting a block to the blockchain:

* Each wallet in the system is classed as a HEX wallet as it has a colour attributed to it using a hexadecimal value. When a user signs a transaction in their crypto-wallet, a hexidecimal hash is generated.
* The AI validator stores information about the current color of the wallet.
* The block builder divides the transaction hash into six parts. It divides each part into two and adds them until it gets a number of one character. Thus, it receives six numbers in HEX, which the block builder glues into a single number, which we term a HEX transaction. Next, the block builder adds the HEX wallet with the HEX transaction and receives a new HEX wallet.
* The HEX of the new HEX wallet and the corresponding transaction hash are sent to the block for which AI validators are selected. AI validators are selected randomly among all those who voted for the AI builder in this round but only to those whose votes were not given to the winning AI builder (the random number is selected according to a special formula). AI validators also form the transaction hash into a transaction HEX and add it to the existing HEX wallet, obtaining a final HEX wallet.
* If the new HEX calculated by the AI validator coincides with the new HEX of the wallet transmitted in the block for all wallets, then the AI validator considers the block valid.
* The colors of the wallets change to those obtained by adding their current colors with HEX transactions.

Lite nodes can also send and receive transactions to power the blockchain wallet application. For a wallet address to become an AI validator, it must satisfy the following conditions:

* In the last N hours there have been transactions at the wallet address (I.E., the wallet is active);
* the wallet address contains the N-amount of the native currency of the blockchain network.

The figure below illustrates how PoAI determines which validators are eligible to vote in the round:

<div>
<img src="/img/Screenshot 2024-05-01 at 9.04.40 AM.png" alt="PoAI Validator Selection"/>
</div>
