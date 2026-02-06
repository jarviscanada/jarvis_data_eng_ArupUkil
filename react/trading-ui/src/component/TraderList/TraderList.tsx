import { Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTrashCan as deleteIcon } from "@fortawesome/free-solid-svg-icons";
import "./TraderList.scss";
import type { Trader } from "../../types/trading";

type Props = {
  traders: Trader[];
  loading?: boolean;
  onTraderDeleteClick: (id: number) => void;
  onTraderRowClick: (id: number) => void;
};

export default function TraderList({
  traders,
  loading,
  onTraderDeleteClick,
  onTraderRowClick,
}: Props) {
  const columns: ColumnsType<Trader> = [
    {
      title: "First Name",
      dataIndex: "firstName",
      key: "firstName",
      sorter: (a, b) => a.firstName.localeCompare(b.firstName),
    },
    {
      title: "Last Name",
      dataIndex: "lastName",
      key: "lastName",
      sorter: (a, b) => a.lastName.localeCompare(b.lastName),
    },
    {
      title: "Email",
      dataIndex: "email",
      key: "email",
    },
    {
      title: "Country",
      dataIndex: "country",
      key: "country",
    },
    {
      title: "Date of Birth",
      dataIndex: "dob",
      key: "dob",
      sorter: (a, b) => Date.parse(a.dob) - Date.parse(b.dob),
    },
    {
      title: "Actions",
      key: "actions",
      render: (_value, record) => (
        <div className="trader-delete-icon">
          <FontAwesomeIcon
            icon={deleteIcon}
            onClick={(e) => {
              e.stopPropagation();
              onTraderDeleteClick(record.id);
            }}
          />
        </div>
      ),
    },
  ];

  return (
    <Table<Trader>
      rowKey="id"
      dataSource={traders}
      columns={columns}
      pagination={false}
      loading={loading}
      onRow={(record) => ({
        onClick: () => onTraderRowClick(record.id),
        style: { cursor: "pointer" },
      })}
    />
  );
}

