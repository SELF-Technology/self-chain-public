---
sidebar_label: "AI Block Builder Algorithm"
sidebar_position: 3
---

# AI Block Builder Algorithm

Block builders are full blockchain nodes that store ready-made blocks of the blockchain and participate in forming and packaging new blocks. The block builder's basis is a Machine Learning (ML) model, which has the initial, specified parameters of the methodology for forming effective blocks from existing mempool transactions.

The model can also learn and improve the efficiency of its work in selecting transactions. The block builder considers transactions from the mempool for the block in the following priority (n - number of transactions in the block):

1. Highest price (&gt;0.2n transactions)
2. Lowest price (&gt;0.2n transactions) - to compensate highest prices
3. Average price (&gt;0.5n)
4. Oldest transactions (&gt;0.1n)

One essential aspect of PoAI is making transactions optimized for affordability. This is why the block builder packs the blocks as much as possible and why the blockchain includes a halving algorithm that drives the reliance on transaction efficiency. Due to the dynamic nature of PoAI, the mechanism can sense and respond to attack vectors and attempts at manipulation and adjust accordingly to mitigate threats.

Each block requires a certain volume of Points to be created (this is known as the PointPrice). The goal is to align the number of points for each block with the number of coins generated. For example, 1 point equals 0.001 coins. If the block requires a PointPrice of 10,000, the blockchain generates 10 new coins. When the blockchain reaches a certain amount of total PointPrice spent (for example, 30,000,000,000 points), each point becomes equal to 0.0005 coins. After reaching the second milestone (for example, 60,000,000,000), each point equals 0.00025 coins.

An effective block is any block assembled so that the average PointPrice is close to the actual PointPrice. It includes transactions with the maximum useful information that can be entered into the block. For clarification, ‘useful informationʼ is PointData (the volume of Points). The goal is for the maximum amount of PointData to be included. A limited number of transactions with a higher-than-average PointPrice should reduce the number of users in the system who set a high PointPrice but still favor and compensate them with a limited number of transactions with a low PointPrice. Most of the average PointPrice transactions will be validated, incentivizing users not to set high PointPrices.

In contrast, other networks choose the highest gas fees, leading to a negative user experience. The alternative version provided by PoAI is illustrated below, where the vertical axis is the PointPrice, and the horizontal is the data length. The center of the target is the chosen price by the algorithm:

<div>
<img src="/img/PoAI-Point-Price.png" alt="PoAI PointPrice"/>
</div>

The effectiveness of the ML model depends on the server's technical resources and the frequency of block assembly. A block builder's ML model is trained based on its work in selecting transactions for a block, comparing its results with those of other block builders, and the final voting result, which determines whose block will be sent to the chain.

‍The more often a block builder forms a block, and the more mempool transactions it manages to sort through during the assembly of a new block, the more efficient the block is and the greater the chance that this particular block will be chosen for inclusion in the chain at the next stage. After the generation of the current block is completed and entered into the chain, a timer is started to assemble a new block. During the timer, all block builders are sorting through mempool transactions and selecting the best transactions to form a block. At the end of the timer, all block builders put their blocks forward to a voting process. The voting application includes the following information:

* Block assembly
* Block efficiency (% filling of the block with useful information)
* Income of the block builder during its existence
* Timestamp of the last block assembly
* Percentage of votes out of the total number of attempts
* Percentage of victory in voting out of the total number of people going to vote

The below figure illustrates how PoAI selects which block builders can participate in the round:

<div>
<img src="/img/PoAI-Block-Builder-Selection.png" alt="PoAI Block Builder Selection"/>
</div>
