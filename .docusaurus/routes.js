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
    component: ComponentCreator('/', '868'),
    routes: [
      {
        path: '/',
        component: ComponentCreator('/', '753'),
        routes: [
          {
            path: '/',
            component: ComponentCreator('/', '7ed'),
            routes: [
              {
                path: '/About SELF/creation-brand',
                component: ComponentCreator('/About SELF/creation-brand', 'dbc'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/About SELF/evolution',
                component: ComponentCreator('/About SELF/evolution', 'df0'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/About SELF/long-term',
                component: ComponentCreator('/About SELF/long-term', '75e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/About SELF/manifesto',
                component: ComponentCreator('/About SELF/manifesto', '841'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/About SELF/Media Coverage',
                component: ComponentCreator('/About SELF/Media Coverage', '37f'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/About SELF/self-sov-alternative',
                component: ComponentCreator('/About SELF/self-sov-alternative', '1d2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Building-on-SELF/',
                component: ComponentCreator('/Building-on-SELF/', '588'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Building-on-SELF/api-gateway',
                component: ComponentCreator('/Building-on-SELF/api-gateway', '573'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Building-on-SELF/cloud-integration',
                component: ComponentCreator('/Building-on-SELF/cloud-integration', '4fb'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Building-on-SELF/cloud-integration 2',
                component: ComponentCreator('/Building-on-SELF/cloud-integration 2', '4cb'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Building-on-SELF/getting-started',
                component: ComponentCreator('/Building-on-SELF/getting-started', '54a'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Building-on-SELF/sdk-structure',
                component: ComponentCreator('/Building-on-SELF/sdk-structure', 'df2'),
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
                path: '/Developing SELF/',
                component: ComponentCreator('/Developing SELF/', '87b'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Developing SELF/code-of-conduct',
                component: ComponentCreator('/Developing SELF/code-of-conduct', 'd4b'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Developing SELF/governance',
                component: ComponentCreator('/Developing SELF/governance', 'bf4'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Developing SELF/security',
                component: ComponentCreator('/Developing SELF/security', '03c'),
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
                path: '/Roadmap/Beta Web App',
                component: ComponentCreator('/Roadmap/Beta Web App', '181'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/Developer Incentives',
                component: ComponentCreator('/Roadmap/Developer Incentives', 'e99'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/Introduction',
                component: ComponentCreator('/Roadmap/Introduction', '09b'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/SDK',
                component: ComponentCreator('/Roadmap/SDK', 'b8f'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/SELF OS',
                component: ComponentCreator('/Roadmap/SELF OS', '537'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/Super-App Testnet',
                component: ComponentCreator('/Roadmap/Super-App Testnet', '978'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/Token/Overview',
                component: ComponentCreator('/Roadmap/Token/Overview', 'd2b'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/Token/Smart-Contract-Architecture',
                component: ComponentCreator('/Roadmap/Token/Smart-Contract-Architecture', 'b03'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Roadmap/Token/Tokenomics',
                component: ComponentCreator('/Roadmap/Token/Tokenomics', 'aea'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Cloud-Architecture/Developer-Integration',
                component: ComponentCreator('/Technical Docs/Cloud-Architecture/Developer-Integration', 'd5e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Cloud-Architecture/Overview',
                component: ComponentCreator('/Technical Docs/Cloud-Architecture/Overview', 'df1'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Cloud-Architecture/Running-Your-Own-Node',
                component: ComponentCreator('/Technical Docs/Cloud-Architecture/Running-Your-Own-Node', '4ed'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Constellation/Industry_Validation_Rules',
                component: ComponentCreator('/Technical Docs/Constellation/Industry_Validation_Rules', '678'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Constellation/Overview',
                component: ComponentCreator('/Technical Docs/Constellation/Overview', 'e6f'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Developer Resources/Getting_Started_Testnet',
                component: ComponentCreator('/Technical Docs/Developer Resources/Getting_Started_Testnet', '253'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Developer Resources/PUBLIC_INTERFACES',
                component: ComponentCreator('/Technical Docs/Developer Resources/PUBLIC_INTERFACES', '374'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Grid-Compute/Future-Vision',
                component: ComponentCreator('/Technical Docs/Grid-Compute/Future-Vision', '101'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Integration/Minima Integration',
                component: ComponentCreator('/Technical Docs/Integration/Minima Integration', '396'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Integration/Rosetta API Integration',
                component: ComponentCreator('/Technical Docs/Integration/Rosetta API Integration', '0d2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Performance/Advanced_TPS_Optimization',
                component: ComponentCreator('/Technical Docs/Performance/Advanced_TPS_Optimization', 'd90'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/AI Block Builder Algorithm',
                component: ComponentCreator('/Technical Docs/PoAI/AI Block Builder Algorithm', '013'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/AI-Validator Algorithm',
                component: ComponentCreator('/Technical Docs/PoAI/AI-Validator Algorithm', 'a39'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Color Marker Examples',
                component: ComponentCreator('/Technical Docs/PoAI/Color Marker Examples', '16c'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Color Marker System',
                component: ComponentCreator('/Technical Docs/PoAI/Color Marker System', '353'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Governance_Implementation',
                component: ComponentCreator('/Technical Docs/PoAI/Governance_Implementation', '905'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Proof-of-AI',
                component: ComponentCreator('/Technical Docs/PoAI/Proof-of-AI', '74e'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Taxonomy',
                component: ComponentCreator('/Technical Docs/PoAI/Taxonomy', '7d2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/The Incentive',
                component: ComponentCreator('/Technical Docs/PoAI/The Incentive', '140'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Validation Process',
                component: ComponentCreator('/Technical Docs/PoAI/Validation Process', '050'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/PoAI/Voting Algorithm',
                component: ComponentCreator('/Technical Docs/PoAI/Voting Algorithm', 'c6d'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Security/AI_Capacity_Implementation',
                component: ComponentCreator('/Technical Docs/Security/AI_Capacity_Implementation', '611'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Security/Overview',
                component: ComponentCreator('/Technical Docs/Security/Overview', 'cb9'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Security/Pattern_Analysis_Security',
                component: ComponentCreator('/Technical Docs/Security/Pattern_Analysis_Security', '048'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Security/Post_Quantum_Cryptography',
                component: ComponentCreator('/Technical Docs/Security/Post_Quantum_Cryptography', '662'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/SELF Chain/SELF_Chain_Architecture',
                component: ComponentCreator('/Technical Docs/SELF Chain/SELF_Chain_Architecture', 'ab2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/SELF Chain/User-Instance-Architecture',
                component: ComponentCreator('/Technical Docs/SELF Chain/User-Instance-Architecture', '573'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/SELF Chain/Why Build A Blockchain',
                component: ComponentCreator('/Technical Docs/SELF Chain/Why Build A Blockchain', '410'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Storage/Hybrid_Architecture',
                component: ComponentCreator('/Technical Docs/Storage/Hybrid_Architecture', 'a50'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Storage/Storage_Integration',
                component: ComponentCreator('/Technical Docs/Storage/Storage_Integration', 'd49'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/Technical Docs/Validate/',
                component: ComponentCreator('/Technical Docs/Validate/', '4fc'),
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
