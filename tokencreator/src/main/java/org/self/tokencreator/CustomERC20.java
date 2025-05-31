pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CustomERC20
 * @dev Custom ERC20 token with additional features
 */
contract CustomERC20 is ERC20, Ownable {
    uint256 private _totalSupply;
    string private _description;
    string private _website;
    string private _logoUrl;
    string[] private _features;

    event TokenMetadataUpdated(string description, string website, string logoUrl, string[] features);

    constructor(
        string memory name_,
        string memory symbol_,
        uint8 decimals_,
        uint256 totalSupply_
    ) ERC20(name_, symbol_) {
        _totalSupply = totalSupply_;
        _mint(msg.sender, totalSupply_);
        _setupDecimals(decimals_);
    }

    function decimals() public view virtual override returns (uint8) {
        return super.decimals();
    }

    function totalSupply() public view virtual override returns (uint256) {
        return _totalSupply;
    }

    function updateMetadata(
        string memory description_,
        string memory website_,
        string memory logoUrl_,
        string[] memory features_
    ) public onlyOwner {
        _description = description_;
        _website = website_;
        _logoUrl = logoUrl_;
        _features = features_;
        emit TokenMetadataUpdated(description_, website_, logoUrl_, features_);
    }

    function getMetadata()
        public
        view
        returns (
            string memory,
            string memory,
            string memory,
            string[] memory
        )
    {
        return (_description, _website, _logoUrl, _features);
    }

    function burn(uint256 amount) public {
        _burn(msg.sender, amount);
    }

    function mint(address to, uint256 amount) public onlyOwner {
        _mint(to, amount);
    }
}
