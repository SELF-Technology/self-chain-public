pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Enumerable.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Burnable.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CustomNFT
 * @dev Custom NFT collection with additional features
 */
contract CustomNFT is ERC721, ERC721Enumerable, ERC721Burnable, Ownable {
    string private _description;
    string private _website;
    string private _logoUrl;
    string private _baseURI;
    bool private _isEnumerable;
    bool private _isBurnable;
    bool private _isMintable;

    event CollectionMetadataUpdated(
        string description,
        string website,
        string logoUrl,
        string baseURI,
        bool isEnumerable,
        bool isBurnable,
        bool isMintable
    );

    constructor(string memory name_, string memory symbol_) ERC721(name_, symbol_) {
        _isEnumerable = true;
        _isBurnable = true;
        _isMintable = true;
    }

    function supportsInterface(bytes4 interfaceId)
        public
        view
        virtual
        override(ERC721, ERC721Enumerable)
        returns (bool)
    {
        return super.supportsInterface(interfaceId);
    }

    function _beforeTokenTransfer(
        address from,
        address to,
        uint256 tokenId
    ) internal virtual override(ERC721, ERC721Enumerable) {
        super._beforeTokenTransfer(from, to, tokenId);
    }

    function updateMetadata(
        string memory description_,
        string memory website_,
        string memory logoUrl_,
        string memory baseURI_,
        bool isEnumerable_,
        bool isBurnable_,
        bool isMintable_
    ) public onlyOwner {
        _description = description_;
        _website = website_;
        _logoUrl = logoUrl_;
        _baseURI = baseURI_;
        _isEnumerable = isEnumerable_;
        _isBurnable = isBurnable_;
        _isMintable = isMintable_;
        emit CollectionMetadataUpdated(
            description_,
            website_,
            logoUrl_,
            baseURI_,
            isEnumerable_,
            isBurnable_,
            isMintable_
        );
    }

    function getMetadata()
        public
        view
        returns (
            string memory,
            string memory,
            string memory,
            string memory,
            bool,
            bool,
            bool
        )
    {
        return (
            _description,
            _website,
            _logoUrl,
            _baseURI,
            _isEnumerable,
            _isBurnable,
            _isMintable
        );
    }

    function mint(address to, uint256 tokenId) public onlyOwner {
        require(_isMintable, "Minting is disabled");
        _mint(to, tokenId);
    }

    function burn(uint256 tokenId) public virtual override {
        require(_isBurnable, "Burning is disabled");
        super.burn(tokenId);
    }
}
