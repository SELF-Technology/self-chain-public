import React from 'react';
import ComponentCreator from '@docusaurus/ComponentCreator';

export default [
  {
    path: '/__docusaurus/debug',
    component: ComponentCreator('/__docusaurus/debug', '5ff'),
    exact: true
  },
  {
    path: '/__docusaurus/debug/config',
    component: ComponentCreator('/__docusaurus/debug/config', '5ba'),
    exact: true
  },
  {
    path: '/__docusaurus/debug/content',
    component: ComponentCreator('/__docusaurus/debug/content', 'a2b'),
    exact: true
  },
  {
    path: '/__docusaurus/debug/globalData',
    component: ComponentCreator('/__docusaurus/debug/globalData', 'c3c'),
    exact: true
  },
  {
    path: '/__docusaurus/debug/metadata',
    component: ComponentCreator('/__docusaurus/debug/metadata', '156'),
    exact: true
  },
  {
    path: '/__docusaurus/debug/registry',
    component: ComponentCreator('/__docusaurus/debug/registry', '88c'),
    exact: true
  },
  {
    path: '/__docusaurus/debug/routes',
    component: ComponentCreator('/__docusaurus/debug/routes', '000'),
    exact: true
  },
  {
    path: '/',
    component: ComponentCreator('/', 'ba7'),
    routes: [
      {
        path: '/',
        component: ComponentCreator('/', 'b8b'),
        routes: [
          {
            path: '/',
            component: ComponentCreator('/', '321'),
            routes: [
              {
                path: '/about-self/creation-brand',
                component: ComponentCreator('/about-self/creation-brand', '2e2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/about-self/evolution',
                component: ComponentCreator('/about-self/evolution', '13d'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/about-self/long-term',
                component: ComponentCreator('/about-self/long-term', '96c'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/about-self/manifesto',
                component: ComponentCreator('/about-self/manifesto', 'b23'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/about-self/media-coverage',
                component: ComponentCreator('/about-self/media-coverage', '49c'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/about-self/self-sov-alternative',
                component: ComponentCreator('/about-self/self-sov-alternative', '03e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/about-self/the-story',
                component: ComponentCreator('/about-self/the-story', '7e6'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/building-on-self/',
                component: ComponentCreator('/building-on-self/', '21e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/building-on-self/api-gateway',
                component: ComponentCreator('/building-on-self/api-gateway', '795'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/building-on-self/developer-integration',
                component: ComponentCreator('/building-on-self/developer-integration', 'dc5'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/building-on-self/getting-started',
                component: ComponentCreator('/building-on-self/getting-started', '353'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/building-on-self/mcp-integration',
                component: ComponentCreator('/building-on-self/mcp-integration', '3e6'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/building-on-self/sdk-reference',
                component: ComponentCreator('/building-on-self/sdk-reference', '501'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/category/️-cloud-architecture',
                component: ComponentCreator('/category/️-cloud-architecture', 'ca7'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/developing-self/',
                component: ComponentCreator('/developing-self/', 'a82'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/developing-self/code-of-conduct',
                component: ComponentCreator('/developing-self/code-of-conduct', 'dca'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/developing-self/governance',
                component: ComponentCreator('/developing-self/governance', '7bd'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/developing-self/security',
                component: ComponentCreator('/developing-self/security', '85b'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/project-status/',
                component: ComponentCreator('/project-status/', '1ce'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/beta-web-app',
                component: ComponentCreator('/roadmap/beta-web-app', '354'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/developer-incentives',
                component: ComponentCreator('/roadmap/developer-incentives', '884'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/Introduction',
                component: ComponentCreator('/roadmap/Introduction', '4f0'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/SDK',
                component: ComponentCreator('/roadmap/SDK', '21f'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/self-os',
                component: ComponentCreator('/roadmap/self-os', '897'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/super-app-testnet',
                component: ComponentCreator('/roadmap/super-app-testnet', '43d'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/Token/Overview',
                component: ComponentCreator('/roadmap/Token/Overview', '5af'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/Token/Smart-Contract-Architecture',
                component: ComponentCreator('/roadmap/Token/Smart-Contract-Architecture', '4bc'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/roadmap/Token/Tokenomics',
                component: ComponentCreator('/roadmap/Token/Tokenomics', '141'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Cloud-Architecture/Developer-Integration',
                component: ComponentCreator('/technical-docs/Cloud-Architecture/Developer-Integration', '4d4'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Cloud-Architecture/Overview',
                component: ComponentCreator('/technical-docs/Cloud-Architecture/Overview', '69d'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Cloud-Architecture/Running-Your-Own-Node',
                component: ComponentCreator('/technical-docs/Cloud-Architecture/Running-Your-Own-Node', '33e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Constellation/Industry_Validation_Rules',
                component: ComponentCreator('/technical-docs/Constellation/Industry_Validation_Rules', '556'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Constellation/Overview',
                component: ComponentCreator('/technical-docs/Constellation/Overview', '5a3'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/developer-resources/Getting_Started_Testnet',
                component: ComponentCreator('/technical-docs/developer-resources/Getting_Started_Testnet', 'f88'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/developer-resources/PUBLIC_INTERFACES',
                component: ComponentCreator('/technical-docs/developer-resources/PUBLIC_INTERFACES', 'de1'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Grid-Compute/Future-Vision',
                component: ComponentCreator('/technical-docs/Grid-Compute/Future-Vision', '09a'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Integration/minima-integration',
                component: ComponentCreator('/technical-docs/Integration/minima-integration', 'b1c'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Integration/rosetta-api-integration',
                component: ComponentCreator('/technical-docs/Integration/rosetta-api-integration', '8af'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Performance/Advanced_TPS_Optimization',
                component: ComponentCreator('/technical-docs/Performance/Advanced_TPS_Optimization', '3be'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/ai-block-builder-algorithm',
                component: ComponentCreator('/technical-docs/PoAI/ai-block-builder-algorithm', 'a2d'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/ai-validator-algorithm',
                component: ComponentCreator('/technical-docs/PoAI/ai-validator-algorithm', 'e34'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/color-marker-examples',
                component: ComponentCreator('/technical-docs/PoAI/color-marker-examples', '33e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/color-marker-system',
                component: ComponentCreator('/technical-docs/PoAI/color-marker-system', '02c'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/Governance_Implementation',
                component: ComponentCreator('/technical-docs/PoAI/Governance_Implementation', 'b79'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/Proof-of-AI',
                component: ComponentCreator('/technical-docs/PoAI/Proof-of-AI', '9fd'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/Taxonomy',
                component: ComponentCreator('/technical-docs/PoAI/Taxonomy', '427'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/the-incentive',
                component: ComponentCreator('/technical-docs/PoAI/the-incentive', '9dd'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/validation-process',
                component: ComponentCreator('/technical-docs/PoAI/validation-process', '212'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/PoAI/voting-algorithm',
                component: ComponentCreator('/technical-docs/PoAI/voting-algorithm', '824'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Security/AI_Capacity_Implementation',
                component: ComponentCreator('/technical-docs/Security/AI_Capacity_Implementation', '143'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Security/Overview',
                component: ComponentCreator('/technical-docs/Security/Overview', '608'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Security/Pattern_Analysis_Security',
                component: ComponentCreator('/technical-docs/Security/Pattern_Analysis_Security', '984'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Security/Post_Quantum_Cryptography',
                component: ComponentCreator('/technical-docs/Security/Post_Quantum_Cryptography', 'd59'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/self-chain/SELF_Chain_Architecture',
                component: ComponentCreator('/technical-docs/self-chain/SELF_Chain_Architecture', 'bed'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/self-chain/User-Instance-Architecture',
                component: ComponentCreator('/technical-docs/self-chain/User-Instance-Architecture', '133'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/self-chain/why-build-a-blockchain',
                component: ComponentCreator('/technical-docs/self-chain/why-build-a-blockchain', 'fe1'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Storage/Hybrid_Architecture',
                component: ComponentCreator('/technical-docs/Storage/Hybrid_Architecture', 'b4d'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Storage/Storage_Integration',
                component: ComponentCreator('/technical-docs/Storage/Storage_Integration', '5d2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/technical-docs/Validate/',
                component: ComponentCreator('/technical-docs/Validate/', '994'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/',
                component: ComponentCreator('/', 'bea'),
                exact: true,
                sidebar: "docs"
              }
            ]
          }
        ]
      }
    ]
  },
  {
    path: '*',
    component: ComponentCreator('*'),
  },
];
